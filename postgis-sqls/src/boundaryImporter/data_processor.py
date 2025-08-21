"""
경계 데이터 처리기
"""

import json
from typing import Dict, List, Any, Optional
from .exceptions import ValidationError


class BoundaryDataProcessor:
    """경계 데이터 처리 및 변환 클래스"""
    
    def __init__(self):
        self.processed_count = 0
        self.error_count = 0
        self.errors = []
    
    def process_feature(self, feature: Dict[str, Any]) -> Optional[Dict[str, Any]]:
        """
        개별 피처를 데이터베이스 삽입용으로 처리
        
        Args:
            feature: GeoJSON 피처
            
        Returns:
            Dict 또는 None: 처리된 데이터 (오류 시 None)
        """
        try:
            properties = feature.get('properties', {})
            geometry = feature.get('geometry', {})
            
            # 필수 속성 확인
            required_props = ['adm_nm']
            for prop in required_props:
                if not properties.get(prop):
                    raise ValidationError(f"필수 속성 누락: {prop}")
            
            # 기하 정보 JSON 문자열로 변환
            geometry_json = json.dumps(geometry)
            
            # 데이터 준비
            processed_data = {
                'adm_nm': properties.get('adm_nm', ''),
                'adm_cd': properties.get('adm_cd', ''),
                'adm_cd2': properties.get('adm_cd2', ''),
                'sido': properties.get('sido', ''),
                'sgg': properties.get('sgg', ''),
                'sidonm': properties.get('sidonm', ''),
                'sggnm': properties.get('sggnm', ''),
                'geometry_json': geometry_json
            }
            
            # 데이터 검증
            self._validate_processed_data(processed_data)
            
            self.processed_count += 1
            return processed_data
            
        except Exception as e:
            self.error_count += 1
            error_msg = f"피처 처리 오류 ({properties.get('adm_nm', 'Unknown')}): {e}"
            self.errors.append(error_msg)
            return None
    
    def _validate_processed_data(self, data: Dict[str, Any]):
        """
        처리된 데이터 검증
        
        Args:
            data: 검증할 데이터
            
        Raises:
            ValidationError: 검증 실패 시
        """
        # 행정구역명 검증
        if not data['adm_nm'] or len(data['adm_nm']) > 100:
            raise ValidationError("행정구역명이 유효하지 않습니다")
        
        # 기하 정보 검증
        try:
            geom_data = json.loads(data['geometry_json'])
            if not geom_data.get('type') or not geom_data.get('coordinates'):
                raise ValidationError("기하 정보가 유효하지 않습니다")
        except json.JSONDecodeError:
            raise ValidationError("기하 정보 JSON 파싱 실패")
    
    def process_features_batch(self, features: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """
        여러 피처를 배치로 처리
        
        Args:
            features: 피처 목록
            
        Returns:
            List[Dict]: 처리된 데이터 목록
        """
        processed_features = []
        
        for feature in features:
            processed = self.process_feature(feature)
            if processed:
                processed_features.append(processed)
        
        return processed_features
    
    def get_statistics(self) -> Dict[str, Any]:
        """
        처리 통계 반환
        
        Returns:
            Dict: 처리 통계
        """
        return {
            'processed_count': self.processed_count,
            'error_count': self.error_count,
            'success_rate': (self.processed_count / (self.processed_count + self.error_count) * 100) 
                           if (self.processed_count + self.error_count) > 0 else 0,
            'errors': self.errors.copy()
        }
    
    def reset_statistics(self):
        """통계 초기화"""
        self.processed_count = 0
        self.error_count = 0
        self.errors = []
    
    def analyze_features(self, features: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        피처 데이터 분석
        
        Args:
            features: 분석할 피처 목록
            
        Returns:
            Dict: 분석 결과
        """
        if not features:
            return {}
        
        # 속성 분석
        all_properties = set()
        sido_count = {}
        sgg_count = {}
        
        for feature in features:
            props = feature.get('properties', {})
            all_properties.update(props.keys())
            
            # 시도별 통계
            sido = props.get('sidonm', 'Unknown')
            sido_count[sido] = sido_count.get(sido, 0) + 1
            
            # 시군구별 통계
            sgg = props.get('sggnm', 'Unknown')
            sgg_count[sgg] = sgg_count.get(sgg, 0) + 1
        
        # 상위 항목들
        top_sido = sorted(sido_count.items(), key=lambda x: x[1], reverse=True)[:10]
        top_sgg = sorted(sgg_count.items(), key=lambda x: x[1], reverse=True)[:20]
        
        return {
            'total_features': len(features),
            'unique_properties': sorted(all_properties),
            'sido_count': len(sido_count),
            'sgg_count': len(sgg_count),
            'top_sido': top_sido,
            'top_sgg': top_sgg,
            'sample_feature': features[0] if features else None
        }
