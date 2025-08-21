"""
sqlClient 예외 클래스들
"""


class DatabaseError(Exception):
    """데이터베이스 관련 일반 오류"""
    pass


class ConnectionError(DatabaseError):
    """데이터베이스 연결 오류"""
    pass


class QueryError(DatabaseError):
    """SQL 쿼리 실행 오류"""
    pass


class FileError(DatabaseError):
    """SQL 파일 관련 오류"""
    pass
