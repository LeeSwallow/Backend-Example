"""
데이터베이스 클라이언트
"""

import os
from pathlib import Path
from typing import Dict, List, Optional, Any, Union
import psycopg2
from psycopg2.extras import RealDictCursor
from psycopg2 import sql

from .config import DatabaseConfig
from .exceptions import ConnectionError, QueryError, FileError


class DatabaseClient:
    """PostgreSQL/PostGIS 데이터베이스 클라이언트"""
    
    def __init__(self, config: Optional[DatabaseConfig] = None):
        """
        초기화
        
        Args:
            config: 데이터베이스 설정 (None이면 기본 설정 사용)
        """
        self.config = config or DatabaseConfig()
        self.connection = None
        self._connected = False
    
    def connect(self) -> bool:
        """
        데이터베이스 연결
        
        Returns:
            bool: 연결 성공 여부
            
        Raises:
            ConnectionError: 연결 실패 시
        """
        try:
            self.connection = psycopg2.connect(**self.config.config)
            self.connection.autocommit = False
            self._connected = True
            return True
        except psycopg2.Error as e:
            raise ConnectionError(f"데이터베이스 연결 실패: {e}")
    
    def disconnect(self):
        """데이터베이스 연결 종료"""
        if self.connection:
            self.connection.close()
            self.connection = None
            self._connected = False
    
    def is_connected(self) -> bool:
        """연결 상태 확인"""
        return self._connected and self.connection is not None
    
    def execute_query(self, query: str, params: Optional[Dict[str, Any]] = None) -> List[Dict[str, Any]]:
        """
        SELECT 쿼리 실행
        
        Args:
            query: SQL 쿼리
            params: 쿼리 파라미터
            
        Returns:
            List[Dict]: 쿼리 결과
            
        Raises:
            QueryError: 쿼리 실행 실패 시
        """
        if not self.is_connected():
            raise ConnectionError("데이터베이스에 연결되지 않음")
        
        try:
            with self.connection.cursor(cursor_factory=RealDictCursor) as cursor:
                cursor.execute(query, params)
                return [dict(row) for row in cursor.fetchall()]
        except psycopg2.Error as e:
            raise QueryError(f"쿼리 실행 실패: {e}")
    
    def execute_command(self, command: str, params: Optional[Dict[str, Any]] = None) -> int:
        """
        INSERT/UPDATE/DELETE 명령 실행
        
        Args:
            command: SQL 명령
            params: 명령 파라미터
            
        Returns:
            int: 영향받은 행 수
            
        Raises:
            QueryError: 명령 실행 실패 시
        """
        if not self.is_connected():
            raise ConnectionError("데이터베이스에 연결되지 않음")
        
        try:
            with self.connection.cursor() as cursor:
                cursor.execute(command, params)
                self.connection.commit()
                return cursor.rowcount
        except psycopg2.Error as e:
            self.connection.rollback()
            raise QueryError(f"명령 실행 실패: {e}")
    
    def execute_script(self, script: str) -> bool:
        """
        멀티라인 SQL 스크립트 실행
        
        Args:
            script: SQL 스크립트
            
        Returns:
            bool: 실행 성공 여부
            
        Raises:
            QueryError: 스크립트 실행 실패 시
        """
        if not self.is_connected():
            raise ConnectionError("데이터베이스에 연결되지 않음")
        
        try:
            with self.connection.cursor() as cursor:
                cursor.execute(script)
                self.connection.commit()
                return True
        except psycopg2.Error as e:
            self.connection.rollback()
            raise QueryError(f"스크립트 실행 실패: {e}")
    
    def execute_sql_file(self, file_path: Union[str, Path]) -> bool:
        """
        SQL 파일 실행
        
        Args:
            file_path: SQL 파일 경로
            
        Returns:
            bool: 실행 성공 여부
            
        Raises:
            FileError: 파일 관련 오류
            QueryError: SQL 실행 오류
        """
        file_path = Path(file_path)
        
        if not file_path.exists():
            raise FileError(f"SQL 파일을 찾을 수 없습니다: {file_path}")
        
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                sql_content = f.read()
            
            return self.execute_script(sql_content)
            
        except UnicodeDecodeError:
            # UTF-8 실패 시 다른 인코딩 시도
            encodings = ['cp949', 'euc-kr', 'latin1']
            for encoding in encodings:
                try:
                    with open(file_path, 'r', encoding=encoding) as f:
                        sql_content = f.read()
                    break
                except UnicodeDecodeError:
                    continue
            else:
                raise FileError(f"SQL 파일 인코딩을 읽을 수 없습니다: {file_path}")
            
            return self.execute_script(sql_content)
            
        except Exception as e:
            raise FileError(f"SQL 파일 읽기 실패: {e}")
    
    def table_exists(self, table_name: str) -> bool:
        """
        테이블 존재 여부 확인
        
        Args:
            table_name: 테이블 이름
            
        Returns:
            bool: 테이블 존재 여부
        """
        query = """
            SELECT EXISTS (
                SELECT FROM information_schema.tables 
                WHERE table_name = %(table_name)s
            )
        """
        result = self.execute_query(query, {'table_name': table_name})
        return result[0]['exists'] if result else False
    
    def get_table_info(self, table_name: str) -> List[Dict[str, Any]]:
        """
        테이블 구조 정보 조회
        
        Args:
            table_name: 테이블 이름
            
        Returns:
            List[Dict]: 컬럼 정보
        """
        query = """
            SELECT 
                column_name,
                data_type,
                is_nullable,
                column_default
            FROM information_schema.columns 
            WHERE table_name = %(table_name)s
            ORDER BY ordinal_position
        """
        return self.execute_query(query, {'table_name': table_name})
    
    def __enter__(self):
        """Context manager 진입"""
        self.connect()
        return self
    
    def __exit__(self, exc_type, exc_val, exc_tb):
        """Context manager 종료"""
        self.disconnect()
