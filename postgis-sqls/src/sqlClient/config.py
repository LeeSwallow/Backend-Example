"""
데이터베이스 설정 관리
"""

import os
from typing import Dict, Optional
from dotenv import load_dotenv


class DatabaseConfig:
    """데이터베이스 연결 설정 클래스"""
    
    def __init__(self, env_file: Optional[str] = None):
        """
        초기화
        
        Args:
            env_file: .env 파일 경로 (None이면 기본 .env 파일 사용)
        """
        # .env 파일 로드
        if env_file:
            load_dotenv(env_file)
        else:
            load_dotenv()
        
        self._config = self._load_config()
    
    def _load_config(self) -> Dict[str, str]:
        """환경변수에서 데이터베이스 설정 로드"""
        return {
            'host': os.getenv('DB_HOST', 'localhost'),
            'port': os.getenv('DB_PORT', '5432'),
            'database': os.getenv('DB_NAME', 'postgres'),
            'user': os.getenv('DB_USER', 'postgres'),
            'password': os.getenv('DB_PASSWORD', 'postgres')
        }
    
    @property
    def config(self) -> Dict[str, str]:
        """데이터베이스 설정 반환"""
        return self._config.copy()
    
    @property
    def connection_string(self) -> str:
        """PostgreSQL 연결 문자열 반환"""
        config = self._config
        return (f"postgresql://{config['user']}:{config['password']}"
                f"@{config['host']}:{config['port']}/{config['database']}")
    
    def __str__(self) -> str:
        """설정 정보 문자열 반환 (비밀번호는 마스킹)"""
        config = self._config.copy()
        config['password'] = '*' * len(config['password'])
        return f"DatabaseConfig({config})"
