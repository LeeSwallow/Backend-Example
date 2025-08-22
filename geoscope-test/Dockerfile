# PostgreSQL 15 공식 이미지 기반
FROM postgres:15

# PostGIS 설치
RUN apt-get update && \
    apt-get install -y postgis postgresql-15-postgis-3 && \
    rm -rf /var/lib/apt/lists/*

# 초기 SQL 파일 복사 (컨테이너 내 /docker-entrypoint-initdb.d/ 폴더에 위치시키면
# 컨테이너 최초 실행시 자동 실행됨)

COPY init-postgis.sql /docker-entrypoint-initdb.d/init.sql

# 9MB 시군구 데이터 파일 복사 (실행은 별도 명령으로)
RUN mkdir -p /extra && chmod 777 /extra
COPY sigungu_data.sql /extra/sigungu_data.sql

# 환경 변수 설정 (필요 시 변경)
ENV POSTGRES_DB=postgres
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=testpass

