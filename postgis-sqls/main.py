#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
한국 행정동 경계 데이터 PostGIS 임포트 메인 모듈

패키지 구조:
- sqlClient: 데이터베이스 연결 및 SQL 실행
- boundaryImporter: GeoJSON 데이터 처리 및 임포트

사용법:
    python main.py [command] [options]
    
명령어:
    setup     - 데이터베이스 테이블 생성
    import    - 데이터 임포트
    analyze   - 데이터 분석 (임포트 없이)
    verify    - 임포트된 데이터 검증
"""

import sys
import argparse
from pathlib import Path

from src.sqlClient import DatabaseClient, DatabaseConfig
from src.boundaryImporter import BoundaryImporter


class Application:
    """메인 애플리케이션 클래스"""
    
    def __init__(self):
        self.db_config = DatabaseConfig()
        self.db_client = None
        self.importer = None
        
        # 기본 설정
        self.default_geojson_file = Path("data/HangJeongDong_ver20250401.geojson")
        self.default_table_sql = Path("sql/create_hangjungdong_table.sql")
        self.default_table_name = "hangjungdong"
    
    def setup_database(self):
        """데이터베이스 및 클라이언트 설정"""
        print("🔧 데이터베이스 설정")
        print(f"   설정: {self.db_config}")
        
        self.db_client = DatabaseClient(self.db_config)
        self.importer = BoundaryImporter(self.db_client)
        
        # 연결 테스트
        try:
            self.db_client.connect()
            print("✅ 데이터베이스 연결 성공")
            return True
        except Exception as e:
            print(f"❌ 데이터베이스 연결 실패: {e}")
            return False
    
    def command_setup(self, args):
        """setup 명령: 데이터베이스 테이블 생성"""
        print("🚀 데이터베이스 테이블 생성")
        print("=" * 50)
        
        if not self.setup_database():
            return False
        
        try:
            success = True
            
            # 커스텀 SQL 파일 사용
            if args.sql_file:
                success = self._create_table_from_sql(args.sql_file)
            else:
                # 테이블 타입별 생성
                if args.table == 'all':
                    tables = ['hangjungdong', 'sigungu', 'sido']
                else:
                    tables = [args.table]
                
                for table_type in tables:
                    print(f"\n📋 {table_type} 테이블 생성 중...")
                    if not self._create_table_by_type(table_type):
                        success = False
                        break
            
            # 시도 데이터 자동 생성
            if success and args.create_sido_from_data:
                print(f"\n🗺️  기존 데이터에서 시도 경계 자동 생성...")
                success = self._create_sido_from_hangjungdong()
            
            return success
            
        except Exception as e:
            print(f"❌ 테이블 생성 실패: {e}")
            return False
        finally:
            self.db_client.disconnect()
    
    def _create_table_from_sql(self, sql_file: Path) -> bool:
        """SQL 파일에서 테이블 생성"""
        if not sql_file.exists():
            print(f"❌ SQL 파일을 찾을 수 없습니다: {sql_file}")
            return False
        
        print(f"📁 SQL 파일 실행: {sql_file}")
        self.db_client.execute_sql_file(sql_file)
        print("✅ 테이블 생성 완료")
        return True
    
    def _create_table_by_type(self, table_type: str) -> bool:
        """테이블 타입별 생성"""
        sql_files = {
            'hangjungdong': Path("sql/create_hangjungdong_table.sql"),
            'sigungu': Path("sql/create_sigungu_table.sql"),
            'sido': Path("sql/create_sido_boundary_table.sql")
        }
        
        sql_file = sql_files.get(table_type)
        if not sql_file or not sql_file.exists():
            print(f"❌ {table_type} 테이블용 SQL 파일을 찾을 수 없습니다: {sql_file}")
            return False
        
        success = self._create_table_from_sql(sql_file)
        
        # 테이블 정보 확인
        if success and self.db_client.table_exists(table_type):
            table_info = self.db_client.get_table_info(table_type)
            print(f"📋 테이블 '{table_type}' 구조:")
            for col in table_info[:5]:  # 처음 5개 컬럼만 표시
                print(f"   {col['column_name']}: {col['data_type']}")
            if len(table_info) > 5:
                print(f"   ... 외 {len(table_info)-5}개 컬럼")
        
        return success
    
    def _create_sido_from_hangjungdong(self) -> bool:
        """기존 행정동 데이터에서 시도 경계 자동 생성"""
        try:
            import geopandas as gpd
            import pandas as pd
        except ImportError:
            print("❌ geopandas 패키지가 필요합니다: uv add geopandas")
            return False
        
        try:
            # 1. 행정동 데이터 확인
            if not self.db_client.table_exists('hangjungdong'):
                print("❌ hangjungdong 테이블이 존재하지 않습니다. 먼저 데이터를 임포트하세요.")
                return False
            
            # 2. 데이터 조회
            print("   행정동 데이터 조회 중...")
            query = """
                SELECT sidonm, sido, 
                       ST_AsGeoJSON(geom) as geometry_json
                FROM hangjungdong 
                WHERE sidonm IS NOT NULL 
                  AND geom IS NOT NULL
            """
            
            rows = self.db_client.execute_query(query)
            if not rows:
                print("❌ 행정동 데이터가 없습니다.")
                return False
            
            print(f"   {len(rows):,}개 행정동 데이터 로드됨")
            
            # 3. 시도별 병합
            print("   시도별 경계 병합 중...")
            
            # GeoDataFrame 생성
            import json
            from shapely.geometry import shape
            from shapely.ops import unary_union
            
            sido_groups = {}
            for row in rows:
                sido_name = row['sidonm']
                geom = shape(json.loads(row['geometry_json']))
                
                if sido_name not in sido_groups:
                    sido_groups[sido_name] = {
                        'sido_code': row['sido'],
                        'geometries': []
                    }
                sido_groups[sido_name]['geometries'].append(geom)
            
            # 4. 시도 데이터 생성 및 삽입
            print("   시도 경계 데이터 생성 중...")
            
            for sido_name, data in sido_groups.items():
                # 병합된 geometry 생성
                merged_geom = unary_union(data['geometries'])
                
                # WKT 형태로 변환
                geom_wkt = merged_geom.wkt
                
                # 데이터베이스에 삽입
                insert_query = """
                    INSERT INTO sido_boundary (sido_code, sido_name, geom)
                    VALUES (%(sido_code)s, %(sido_name)s, ST_GeomFromText(%(geom_wkt)s, 4326))
                    ON CONFLICT (sido_code) DO UPDATE SET 
                        sido_name = EXCLUDED.sido_name,
                        geom = EXCLUDED.geom,
                        updated_at = NOW()
                """
                
                self.db_client.execute_command(insert_query, {
                    'sido_code': data['sido_code'],
                    'sido_name': sido_name,
                    'geom_wkt': geom_wkt
                })
            
            # 5. 결과 확인
            result_query = """
                SELECT 
                    sb.sido_name,
                    COUNT(h.id) as dong_count,
                    COUNT(DISTINCT h.sggnm) as sigungu_count,
                    ROUND(CAST(ST_Area(ST_Transform(sb.geom, 5179)) / 1000000 AS NUMERIC), 2) as area_km2
                FROM sido_boundary sb
                LEFT JOIN hangjungdong h ON h.sidonm = sb.sido_name
                GROUP BY sb.sido_name, sb.geom
                ORDER BY dong_count DESC
            """
            results = self.db_client.execute_query(result_query)
            
            print(f"✅ 시도 경계 생성 완료! ({len(results)}개 시도)")
            print(f"📊 상위 5개 시도:")
            for row in results[:5]:
                print(f"   {row['sido_name']}: {row['dong_count']}개 동, {row['area_km2']}km²")
            
            return True
            
        except Exception as e:
            print(f"❌ 시도 경계 생성 실패: {e}")
            return False
    
    def command_import(self, args):
        """import 명령: 데이터 임포트"""
        if args.level == 'hangjungdong':
            return self._import_hangjungdong(args)
        elif args.level == 'sigungu':
            return self._import_sigungu(args)
        else:
            print(f"❌ 알 수 없는 데이터 레벨: {args.level}")
            return False
    
    def _import_hangjungdong(self, args):
        """행정동 데이터 임포트"""
        print("🚀 한국 행정동 경계 데이터 임포트")
        print("=" * 50)
        
        if not self.setup_database():
            return False
        
        try:
            # 파일 확인
            geojson_file = args.geojson_file or self.default_geojson_file
            
            if not geojson_file.exists():
                print(f"❌ GeoJSON 파일을 찾을 수 없습니다: {geojson_file}")
                return False
            
            # 임포트 실행
            result = self.importer.import_from_file(
                file_path=geojson_file,
                table_name=args.table_name or self.default_table_name,
                clear_existing=not args.append
            )
            
            if result['success']:
                print(f"\n🎉 행정동 임포트 성공!")
                return True
            else:
                print(f"\n❌ 행정동 임포트 실패: {result.get('message', 'Unknown error')}")
                return False
                
        except Exception as e:
            print(f"❌ 행정동 임포트 중 오류: {e}")
            return False
        finally:
            self.db_client.disconnect()
    
    def _import_sigungu(self, args):
        """시군구 데이터 임포트 (행정동에서 자동 생성)"""
        print("🚀 시군구 경계 데이터 생성 및 임포트")
        print("=" * 50)
        
        if not self.setup_database():
            return False
        
        try:
            # GeoPandas 임포트 확인
            try:
                import geopandas as gpd
                import pandas as pd
            except ImportError:
                print("❌ geopandas 패키지가 필요합니다: uv add geopandas")
                return False
            
            # 1. 행정동 데이터 확인
            if not self.db_client.table_exists('hangjungdong'):
                print("❌ hangjungdong 테이블이 존재하지 않습니다. 먼저 행정동 데이터를 임포트하세요.")
                return False
            
            # 2. 시군구 테이블 확인
            table_name = args.table_name or 'sigungu'
            if not self.db_client.table_exists(table_name):
                print(f"❌ {table_name} 테이블이 존재하지 않습니다. 먼저 테이블을 생성하세요.")
                print(f"   실행: python main.py setup --table sigungu")
                return False
            
            # 3. 기존 데이터 처리
            if not args.append:
                existing_count = self._get_table_count(table_name)
                if existing_count > 0:
                    print(f"⚠️  기존 {table_name} 데이터 {existing_count:,}개 발견")
                    print(f"   기존 데이터를 삭제하고 새로 생성합니다...")
                    self._clear_table(table_name)
                    print(f"✅ 기존 데이터 삭제 완료")
            
            # 4. 시군구 데이터 생성
            return self._create_sigungu_from_hangjungdong(table_name)
            
        except Exception as e:
            print(f"❌ 시군구 임포트 중 오류: {e}")
            return False
        finally:
            self.db_client.disconnect()
    
    def _get_table_count(self, table_name: str) -> int:
        """테이블 레코드 수 조회"""
        query = f"SELECT COUNT(*) as count FROM {table_name}"
        result = self.db_client.execute_query(query)
        return result[0]['count'] if result else 0
    
    def _clear_table(self, table_name: str):
        """테이블 데이터 삭제"""
        command = f"DELETE FROM {table_name}"
        self.db_client.execute_command(command)
    
    def _create_sigungu_from_hangjungdong(self, table_name: str = 'sigungu') -> bool:
        """기존 행정동 데이터에서 시군구 경계 생성"""
        try:
            # 1. 행정동 데이터 조회
            print("   행정동 데이터 조회 중...")
            query = """
                SELECT sidonm, sggnm, sido, sgg,
                       ST_AsGeoJSON(geom) as geometry_json
                FROM hangjungdong 
                WHERE sidonm IS NOT NULL 
                  AND sggnm IS NOT NULL
                  AND geom IS NOT NULL
            """
            
            rows = self.db_client.execute_query(query)
            if not rows:
                print("❌ 행정동 데이터가 없습니다.")
                return False
            
            print(f"   {len(rows):,}개 행정동 데이터 로드됨")
            
            # 2. 시군구별 그룹화 및 병합
            print("   시군구별 경계 병합 중...")
            
            import json
            from shapely.geometry import shape
            from shapely.ops import unary_union
            
            sigungu_groups = {}
            for row in rows:
                key = f"{row['sidonm']}_{row['sggnm']}"
                geom = shape(json.loads(row['geometry_json']))
                
                if key not in sigungu_groups:
                    sigungu_groups[key] = {
                        'sidonm': row['sidonm'],
                        'sggnm': row['sggnm'],
                        'sido': row['sido'],
                        'sgg': row['sgg'],
                        'geometries': []
                    }
                sigungu_groups[key]['geometries'].append(geom)
            
            # 3. 시군구 데이터 생성 및 삽입
            print(f"   {len(sigungu_groups)}개 시군구 데이터 생성 중...")
            
            success_count = 0
            for i, (key, data) in enumerate(sigungu_groups.items(), 1):
                try:
                    # 병합된 geometry 생성
                    merged_geom = unary_union(data['geometries'])
                    geom_wkt = merged_geom.wkt
                    
                    # 데이터베이스에 삽입
                    insert_query = f"""
                        INSERT INTO {table_name} (sidonm, sggnm, sido, sgg, geom)
                        VALUES (%(sidonm)s, %(sggnm)s, %(sido)s, %(sgg)s, ST_GeomFromText(%(geom_wkt)s, 4326))
                    """
                    
                    self.db_client.execute_command(insert_query, {
                        'sidonm': data['sidonm'],
                        'sggnm': data['sggnm'],
                        'sido': data['sido'],
                        'sgg': data['sgg'],
                        'geom_wkt': geom_wkt
                    })
                    
                    success_count += 1
                    
                    # 진행률 표시
                    if i % 20 == 0 or i == len(sigungu_groups):
                        progress = (i / len(sigungu_groups)) * 100
                        print(f"      진행률: {progress:.1f}% ({i}/{len(sigungu_groups)})")
                        
                except Exception as e:
                    print(f"      ⚠️  {data['sidonm']} {data['sggnm']} 처리 실패: {e}")
            
            # 4. 결과 확인
            result_query = f"""
                SELECT 
                    s.sidonm || ' ' || s.sggnm as 시군구,
                    COUNT(h.id) as 동수,
                    ROUND(CAST(ST_Area(ST_Transform(s.geom, 5179)) / 1000000 AS NUMERIC), 2) as 면적_km2
                FROM {table_name} s
                LEFT JOIN hangjungdong h ON h.sidonm = s.sidonm AND h.sggnm = s.sggnm
                GROUP BY s.sidonm, s.sggnm, s.geom
                ORDER BY 동수 DESC
                LIMIT 10
            """
            results = self.db_client.execute_query(result_query)
            
            print(f"\n✅ 시군구 데이터 생성 완료! ({success_count}개 성공)")
            print(f"📊 상위 10개 시군구:")
            for row in results:
                print(f"   {row['시군구']}: {row['동수']}개 동, {row['면적_km2']}km²")
            
            return True
            
        except Exception as e:
            print(f"❌ 시군구 생성 실패: {e}")
            return False
    
    def command_analyze(self, args):
        """analyze 명령: 데이터 분석"""
        print("🔍 GeoJSON 데이터 분석")
        print("=" * 30)
        
        try:
            # 파일 확인
            geojson_file = args.geojson_file or self.default_geojson_file
            
            if not geojson_file.exists():
                print(f"❌ GeoJSON 파일을 찾을 수 없습니다: {geojson_file}")
                return False
            
            # 임포터 생성 (DB 연결 없이)
            self.importer = BoundaryImporter(None)
            
            # 분석 실행
            result = self.importer.analyze_data(geojson_file)
            
            # 결과 출력
            analysis = result['analysis']
            print(f"📁 파일: {result['file_path']}")
            print(f"📊 총 피처 수: {analysis['total_features']:,}개")
            print(f"📋 속성 개수: {len(analysis['unique_properties'])}")
            print(f"🗺️  시도 개수: {analysis['sido_count']}")
            print(f"🏘️  시군구 개수: {analysis['sgg_count']}")
            
            print(f"\n📍 상위 5개 시도:")
            for sido, count in analysis['top_sido'][:5]:
                print(f"   {sido}: {count:,}개")
            
            print(f"\n🏘️  상위 10개 시군구:")
            for sgg, count in analysis['top_sgg'][:10]:
                print(f"   {sgg}: {count:,}개")
            
            return True
            
        except Exception as e:
            print(f"❌ 분석 중 오류: {e}")
            return False
    
    def command_verify(self, args):
        """verify 명령: 임포트된 데이터 검증"""
        print("🔍 임포트된 데이터 검증")
        print("=" * 30)
        
        if not self.setup_database():
            return False
        
        try:
            table_name = args.table_name or self.default_table_name
            
            # 테이블 존재 확인
            if not self.db_client.table_exists(table_name):
                print(f"❌ 테이블 '{table_name}'이 존재하지 않습니다")
                return False
            
            # 기본 통계
            total_query = f"SELECT COUNT(*) as count FROM {table_name}"
            total_result = self.db_client.execute_query(total_query)
            total_count = total_result[0]['count']
            
            print(f"📊 총 레코드 수: {total_count:,}개")
            
            if total_count == 0:
                print("⚠️  테이블이 비어있습니다")
                return True
            
            # 테이블별 다른 통계 쿼리
            if table_name == 'hangjungdong':
                # 행정동 테이블: 시도별 통계
                sido_query = f"""
                    SELECT sidonm, COUNT(*) as count 
                    FROM {table_name} 
                    WHERE sidonm IS NOT NULL 
                    GROUP BY sidonm 
                    ORDER BY count DESC 
                    LIMIT 10
                """
                sido_stats = self.db_client.execute_query(sido_query)
                
                print(f"\n📍 상위 10개 시도:")
                for row in sido_stats:
                    print(f"   {row['sidonm']}: {row['count']:,}개")
                
                # 샘플 데이터
                sample_query = f"""
                    SELECT adm_nm, sidonm, sggnm 
                    FROM {table_name} 
                    ORDER BY RANDOM() 
                    LIMIT 5
                """
                samples = self.db_client.execute_query(sample_query)
                
                print(f"\n📋 랜덤 샘플 데이터:")
                for row in samples:
                    print(f"   {row['adm_nm']}")
                    
            elif table_name == 'sigungu':
                # 시군구 테이블: 시도별 시군구 수
                sido_query = f"""
                    SELECT sidonm, COUNT(*) as count 
                    FROM {table_name} 
                    WHERE sidonm IS NOT NULL 
                    GROUP BY sidonm 
                    ORDER BY count DESC 
                    LIMIT 10
                """
                sido_stats = self.db_client.execute_query(sido_query)
                
                print(f"\n📍 상위 10개 시도 (시군구 수):")
                for row in sido_stats:
                    print(f"   {row['sidonm']}: {row['count']:,}개 시군구")
                
                # 샘플 데이터
                sample_query = f"""
                    SELECT sidonm || ' ' || sggnm as 시군구
                    FROM {table_name} 
                    ORDER BY RANDOM() 
                    LIMIT 5
                """
                samples = self.db_client.execute_query(sample_query)
                
                print(f"\n📋 랜덤 샘플 데이터:")
                for row in samples:
                    print(f"   {row['시군구']}")
                    
            elif table_name == 'sido_boundary':
                # 시도 테이블: 전체 시도 목록
                sido_query = f"""
                    SELECT sido_name, sido_code
                    FROM {table_name} 
                    ORDER BY sido_name
                """
                sido_stats = self.db_client.execute_query(sido_query)
                
                print(f"\n📍 전체 시도 목록:")
                for row in sido_stats:
                    print(f"   {row['sido_name']} ({row['sido_code']})")
                    
            else:
                # 기타 테이블: 기본 샘플링
                print(f"\n📋 기본 샘플 데이터:")
                table_info = self.db_client.get_table_info(table_name)
                if table_info:
                    first_cols = [col['column_name'] for col in table_info[:3] 
                                if col['column_name'] != 'geom']
                    if first_cols:
                        cols_str = ', '.join(first_cols)
                        sample_query = f"SELECT {cols_str} FROM {table_name} LIMIT 5"
                        samples = self.db_client.execute_query(sample_query)
                        for row in samples:
                            print(f"   {dict(row)}")
            
            # 기하 정보 확인 (모든 테이블 공통)
            geom_query = f"""
                SELECT 
                    COUNT(*) as total,
                    COUNT(geom) as with_geom,
                    ST_GeometryType(geom) as geom_type
                FROM {table_name}
                WHERE geom IS NOT NULL
                GROUP BY ST_GeometryType(geom)
                LIMIT 5
            """
            geom_stats = self.db_client.execute_query(geom_query)
            
            print(f"\n🗺️  기하 정보:")
            for row in geom_stats:
                print(f"   {row['geom_type']}: {row['with_geom']:,}개")
            
            return True
            
        except Exception as e:
            print(f"❌ 검증 중 오류: {e}")
            return False
        finally:
            self.db_client.disconnect()


def main():
    """메인 함수"""
    parser = argparse.ArgumentParser(
        description="한국 행정동 경계 데이터 PostGIS 임포트 도구",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
예시:
    # 테이블 생성
    python main.py setup                              # 행정동 테이블만 생성
    python main.py setup --table sido                # 시도 테이블만 생성  
    python main.py setup --table all                 # 모든 테이블 생성
    python main.py setup --create-sido-from-data     # 시도 경계 자동 생성
    
    # 데이터 처리
    python main.py import                             # 행정동 데이터 임포트
    python main.py import --level sigungu             # 시군구 데이터 생성
    python main.py analyze                            # 데이터 분석
    python main.py verify                             # 데이터 검증
        """
    )
    
    subparsers = parser.add_subparsers(dest='command', help='사용 가능한 명령어')
    
    # setup 명령
    setup_parser = subparsers.add_parser('setup', help='데이터베이스 테이블 생성')
    setup_parser.add_argument('--table', choices=['hangjungdong', 'sigungu', 'sido', 'all'], 
                             default='hangjungdong', help='생성할 테이블 선택')
    setup_parser.add_argument('--sql-file', type=Path, 
                             help='커스텀 SQL 파일 경로 (기본 테이블 대신 사용)')
    setup_parser.add_argument('--create-sido-from-data', action='store_true',
                             help='기존 행정동 데이터에서 시도 경계 자동 생성')
    
    # import 명령
    import_parser = subparsers.add_parser('import', help='데이터 임포트')
    import_parser.add_argument('--level', choices=['hangjungdong', 'sigungu'], 
                              default='hangjungdong', help='임포트할 데이터 레벨')
    import_parser.add_argument('--geojson-file', type=Path,
                              help='GeoJSON 파일 경로 (hangjungdong만)')
    import_parser.add_argument('--table-name', 
                              help='대상 테이블명')
    import_parser.add_argument('--append', action='store_true',
                              help='기존 데이터 유지 (삭제하지 않음)')
    import_parser.add_argument('--create-from-hangjungdong', action='store_true',
                              help='기존 행정동 데이터에서 시군구 생성')
    
    # analyze 명령
    analyze_parser = subparsers.add_parser('analyze', help='데이터 분석')
    analyze_parser.add_argument('--geojson-file', type=Path,
                               help='GeoJSON 파일 경로')
    
    # verify 명령
    verify_parser = subparsers.add_parser('verify', help='데이터 검증')
    verify_parser.add_argument('--table-name',
                              help='검증할 테이블명 (기본: hangjungdong)')
    
    args = parser.parse_args()
    
    if not args.command:
        parser.print_help()
        sys.exit(1)
    
    # 애플리케이션 실행
    app = Application()
    
    try:
        if args.command == 'setup':
            success = app.command_setup(args)
        elif args.command == 'import':
            success = app.command_import(args)
        elif args.command == 'analyze':
            success = app.command_analyze(args)
        elif args.command == 'verify':
            success = app.command_verify(args)
        else:
            print(f"❌ 알 수 없는 명령어: {args.command}")
            success = False
        
        sys.exit(0 if success else 1)
        
    except KeyboardInterrupt:
        print("\n⚠️  사용자가 작업을 중단했습니다")
        sys.exit(1)
    except Exception as e:
        print(f"\n❌ 예상치 못한 오류: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()