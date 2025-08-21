"""
boundaryImporter 예외 클래스들
"""


class ImportError(Exception):
    """임포트 관련 일반 오류"""
    pass


class DataError(ImportError):
    """데이터 처리 오류"""
    pass


class FileLoadError(ImportError):
    """파일 로드 오류"""
    pass


class ValidationError(ImportError):
    """데이터 검증 오류"""
    pass
