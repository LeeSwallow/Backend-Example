"""
GeoJSON 파일 로더
"""

import json
from pathlib import Path
from typing import Dict, List, Any, Union, Optional

from .exceptions import FileLoadError, DataError


class GeoJSONLoader:
    """GeoJSON 파일 로드 및 파싱 클래스"""
    
    def __init__(self):
        self.data = None
        self.features = []
        self.total_features = 0
    
    def load_file(self, file_path: Union[str, Path]) -> Dict[str, Any]:
        """
        GeoJSON 파일 로드
        
        Args:
            file_path: GeoJSON 파일 경로
            
        Returns:
            Dict: GeoJSON 데이터
            
        Raises:
            FileLoadError: 파일 로드 실패 시
            DataError: 데이터 형식 오류 시
        """
        file_path = Path(file_path)
        
        if not file_path.exists():
            raise FileLoadError(f"파일을 찾을 수 없습니다: {file_path}")
        
        # 여러 인코딩으로 시도
        encodings = ['utf-8', 'utf-8-sig', 'cp949', 'euc-kr', 'latin1']
        
        for encoding in encodings:
            try:
                with open(file_path, 'r', encoding=encoding) as f:
                    data = json.load(f)
                
                # GeoJSON 형식 검증
                self._validate_geojson(data)
                
                self.data = data
                self.features = data.get('features', [])
                self.total_features = len(self.features)
                
                return data
                
            except UnicodeDecodeError:
                continue
            except json.JSONDecodeError as e:
                continue
            except Exception as e:
                raise FileLoadError(f"파일 로드 중 오류 ({encoding}): {e}")
        
        raise FileLoadError(f"모든 인코딩으로 파일 로드 실패: {file_path}")
    
    def _validate_geojson(self, data: Dict[str, Any]):
        """
        GeoJSON 형식 검증
        
        Args:
            data: 검증할 데이터
            
        Raises:
            DataError: GeoJSON 형식이 올바르지 않은 경우
        """
        if not isinstance(data, dict):
            raise DataError("GeoJSON 데이터가 dictionary가 아닙니다")
        
        if data.get('type') != 'FeatureCollection':
            raise DataError("GeoJSON type이 'FeatureCollection'이 아닙니다")
        
        features = data.get('features', [])
        if not isinstance(features, list):
            raise DataError("features가 list가 아닙니다")
        
        if len(features) == 0:
            raise DataError("features가 비어있습니다")
        
        # 첫 번째 feature 검증
        first_feature = features[0]
        if not isinstance(first_feature, dict):
            raise DataError("feature가 dictionary가 아닙니다")
        
        if first_feature.get('type') != 'Feature':
            raise DataError("feature type이 'Feature'가 아닙니다")
        
        if 'properties' not in first_feature:
            raise DataError("feature에 properties가 없습니다")
        
        if 'geometry' not in first_feature:
            raise DataError("feature에 geometry가 없습니다")
    
    def get_features(self) -> List[Dict[str, Any]]:
        """
        피처 목록 반환
        
        Returns:
            List[Dict]: 피처 목록
        """
        return self.features.copy()
    
    def get_feature_count(self) -> int:
        """
        피처 개수 반환
        
        Returns:
            int: 피처 개수
        """
        return self.total_features
    
    def get_sample_feature(self) -> Optional[Dict[str, Any]]:
        """
        샘플 피처 반환 (첫 번째 피처)
        
        Returns:
            Dict 또는 None: 샘플 피처
        """
        return self.features[0] if self.features else None
    
    def get_properties_info(self) -> Dict[str, Any]:
        """
        속성 정보 분석
        
        Returns:
            Dict: 속성 정보 요약
        """
        if not self.features:
            return {}
        
        # 모든 속성 키 수집
        all_keys = set()
        for feature in self.features[:100]:  # 처음 100개만 분석
            properties = feature.get('properties', {})
            all_keys.update(properties.keys())
        
        # 샘플 데이터
        sample = self.features[0].get('properties', {})
        
        return {
            'total_properties': len(all_keys),
            'property_keys': sorted(all_keys),
            'sample_properties': sample
        }
    
    def get_geometry_info(self) -> Dict[str, Any]:
        """
        기하 정보 분석
        
        Returns:
            Dict: 기하 정보 요약
        """
        if not self.features:
            return {}
        
        # 기하 타입 수집
        geometry_types = set()
        for feature in self.features[:100]:  # 처음 100개만 분석
            geometry = feature.get('geometry', {})
            geometry_types.add(geometry.get('type', 'Unknown'))
        
        sample_geom = self.features[0].get('geometry', {})
        
        return {
            'geometry_types': sorted(geometry_types),
            'sample_geometry_type': sample_geom.get('type', 'Unknown'),
            'sample_coordinates_length': len(str(sample_geom.get('coordinates', [])))
        }
