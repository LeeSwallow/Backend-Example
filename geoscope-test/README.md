# PostGIS Docker 이미지 사용 가이드

이 Docker 이미지는 PostgreSQL 15 공식 이미지를 기반으로 PostGIS 확장이 포함되어 있습니다.  
초기 PostGIS 확장 설치용 SQL과 대용량 시군구 데이터 SQL 파일을 포함하며, 대용량 SQL 데이터는 컨테이너 구동 후 수동으로 불러오는 방식을 권장합니다.

---

## 이미지 빌드

```bash
docker build -t my-postgis-image .
```

---

## 컨테이너 실행

```bash
docker run -d -p 5433:5432 --name postgis-server \
-e POSTGRES_PASSWORD=testpass \
my-postgis-image
```

- PostgreSQL 15가 5432 포트로 실행되며, 호스트의 5433 포트와 매핑됩니다.
- 환경변수 `POSTGRES_PASSWORD`를 통해 비밀번호를 설정합니다.

---

## 초기화 스크립트 자동 실행

`/docker-entrypoint-initdb.d/` 디렉토리에 위치한 `init-postgis.sql`은  
컨테이너 최초 실행 시 자동으로 실행되어 PostGIS 확장을 생성합니다.

---

## 대용량 SQL 데이터 수동 적용 (예: 시군구 데이터)

복사된 대용량 SQL 파일(`sigungu_data.sql`)은  
컨테이너 구동 후 수동으로 아래 명령어로 적용하세요.

```bash
docker exec -i postgis-server psql -U postgres -d postgres -f /extra/sigungu_data.sql
```

- `docker exec -i`로 컨테이너 내부 `psql`에 SQL 파일을 전달해 실행합니다.
- 대용량 데이터도 안정적으로 로드할 수 있는 방법입니다.

---

## 접속 정보 예시

- 호스트: `localhost`  
- 포트: `5433`  
- 데이터베이스명: `postgres`  
- 사용자명: `postgres`  
- 비밀번호: `testpass`

---

## 참고 사항

- `postgresql.conf`와 `pg_hba.conf`는 기본 설정으로 외부 접속 가능하게 설정되어 있습니다.  
- 포트 5433이 방화벽 등에서 열려 있어야 클라이언트 접속이 가능합니다.  
- 대용량 SQL 자동 실행 시 메모리 부족이나 시간 초과 문제가 발생할 수 있으므로 수동 적용을 권장합니다.

---

문의나 추가 지원이 필요하면 언제든 연락 주세요.
