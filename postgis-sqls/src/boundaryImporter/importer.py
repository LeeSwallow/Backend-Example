"""
경계 데이터 임포터
"""

from pathlib import Path
from typing import Dict, List, Any, Union, Optional

from ..sqlClient import DatabaseClient
from .geojson_loader import GeoJSONLoader
from .data_processor import BoundaryDataProcessor
from .exceptions import ImportError


class BoundaryImporter:
    """경계 데이터 임포트 메인 클래스"""
    
    def __init__(self, db_client: DatabaseClient):
        """
        초기화
        
        Args:
            db_client: 데이터베이스 클라이언트
        """
        self.db_client = db_client
        self.loader = GeoJSONLoader()
        self.processor = BoundaryDataProcessor()
        
        # 통계
        self.import_stats = {
            'total_features': 0,
            'imported_count': 0,
            'error_count': 0,
            'batch_size': 100
        }
    
    def import_from_file(self, file_path: Union[str, Path], 
                        table_name: str = 'hangjungdong',
                        clear_existing: bool = True) -> Dict[str, Any]:
        """
        파일에서 경계 데이터 임포트
        
        Args:
            file_path: GeoJSON 파일 경로
            table_name: 대상 테이블명
            clear_existing: 기존 데이터 삭제 여부
            
        Returns:
            Dict: 임포트 결과
            
        Raises:
            ImportError: 임포트 실패 시
        """
        try:
            # 1. 파일 로드
            print(f"📁 GeoJSON 파일 로드: {file_path}")
            self.loader.load_file(file_path)
            features = self.loader.get_features()
            self.import_stats['total_features'] = len(features)
            
            print(f"✅ {len(features):,}개 피처 로드 완료")
            
            # 2. 테이블 존재 확인
            if not self.db_client.table_exists(table_name):
                raise ImportError(f"테이블 '{table_name}'이 존재하지 않습니다. "
                                f"먼저 테이블을 생성하세요.")
            
            print(f"✅ 테이블 '{table_name}' 확인 완료")
            
            # 3. 기존 데이터 처리
            if clear_existing:
                existing_count = self._get_table_count(table_name)
                if existing_count > 0:
                    print(f"⚠️  기존 데이터 {existing_count:,}개 발견")
                    if self._confirm_clear_data():
                        self._clear_table(table_name)
                        print(f"✅ 기존 데이터 삭제 완료")
                    else:
                        print("❌ 사용자가 데이터 삭제를 취소했습니다")
                        return {'success': False, 'message': 'User cancelled'}
            
            # 4. 데이터 임포트
            print(f"\n🚀 데이터 임포트 시작")
            result = self._import_features(features, table_name)
            
            # 5. 결과 반환
            print(f"\n📊 임포트 완료")
            print(f"   성공: {result['imported_count']:,}개")
            print(f"   실패: {result['error_count']:,}개")
            
            return result
            
        except Exception as e:
            raise ImportError(f"임포트 실패: {e}")
    
    def _get_table_count(self, table_name: str) -> int:
        """테이블 레코드 수 조회"""
        query = f"SELECT COUNT(*) as count FROM {table_name}"
        result = self.db_client.execute_query(query)
        return result[0]['count'] if result else 0
    
    def _confirm_clear_data(self) -> bool:
        """기존 데이터 삭제 확인"""
        # 실제 환경에서는 사용자 입력을 받지만, 
        # 여기서는 자동으로 승인하도록 설정
        return True
    
    def _clear_table(self, table_name: str):
        """테이블 데이터 삭제"""
        command = f"DELETE FROM {table_name}"
        self.db_client.execute_command(command)
    
    def _import_features(self, features: List[Dict[str, Any]], 
                        table_name: str) -> Dict[str, Any]:
        """피처 데이터 임포트"""
        batch_size = self.import_stats['batch_size']
        imported_count = 0
        error_count = 0
        
        print(f"   배치 크기: {batch_size}")
        print("-" * 50)
        
        # 배치별로 처리
        for i in range(0, len(features), batch_size):
            batch = features[i:i + batch_size]
            batch_result = self._import_batch(batch, table_name)
            
            imported_count += batch_result['imported']
            error_count += batch_result['errors']
            
            # 진행률 표시
            progress = ((i + len(batch)) / len(features)) * 100
            print(f"진행률: {progress:.1f}% ({i + len(batch):,}/{len(features):,}) "
                  f"성공: {imported_count:,}, 실패: {error_count}")
        
        self.import_stats['imported_count'] = imported_count
        self.import_stats['error_count'] = error_count
        
        return {
            'success': True,
            'total_features': len(features),
            'imported_count': imported_count,
            'error_count': error_count,
            'success_rate': (imported_count / len(features)) * 100 if features else 0
        }
    
    def _import_batch(self, batch: List[Dict[str, Any]], 
                     table_name: str) -> Dict[str, int]:
        """배치 데이터 임포트"""
        imported = 0
        errors = 0
        
        for feature in batch:
            try:
                # 데이터 처리
                processed_data = self.processor.process_feature(feature)
                if not processed_data:
                    errors += 1
                    continue
                
                # 데이터베이스 삽입
                self._insert_feature(processed_data, table_name)
                imported += 1
                
            except Exception as e:
                errors += 1
                # 로그는 processor에서 관리됨
        
        return {'imported': imported, 'errors': errors}
    
    def _insert_feature(self, data: Dict[str, Any], table_name: str):
        """개별 피처 데이터베이스 삽입"""
        command = f"""
            INSERT INTO {table_name} (
                adm_nm, adm_cd, adm_cd2, sido, sgg, 
                sidonm, sggnm, geom
            ) VALUES (
                %(adm_nm)s, %(adm_cd)s, %(adm_cd2)s, 
                %(sido)s, %(sgg)s, %(sidonm)s, %(sggnm)s,
                ST_GeomFromGeoJSON(%(geometry_json)s)
            )
        """
        self.db_client.execute_command(command, data)
    
    def get_import_statistics(self) -> Dict[str, Any]:
        """임포트 통계 반환"""
        processor_stats = self.processor.get_statistics()
        
        return {
            'import_stats': self.import_stats.copy(),
            'processor_stats': processor_stats,
            'loader_info': {
                'total_features': self.loader.get_feature_count(),
                'properties_info': self.loader.get_properties_info(),
                'geometry_info': self.loader.get_geometry_info()
            }
        }
    
    def analyze_data(self, file_path: Union[str, Path]) -> Dict[str, Any]:
        """
        데이터 분석 (임포트 없이)
        
        Args:
            file_path: GeoJSON 파일 경로
            
        Returns:
            Dict: 분석 결과
        """
        # 파일 로드
        self.loader.load_file(file_path)
        features = self.loader.get_features()
        
        # 분석 수행
        analysis = self.processor.analyze_features(features)
        loader_info = {
            'properties_info': self.loader.get_properties_info(),
            'geometry_info': self.loader.get_geometry_info()
        }
        
        return {
            'file_path': str(file_path),
            'analysis': analysis,
            'loader_info': loader_info
        }
