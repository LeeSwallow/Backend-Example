# PostGIS 한국 행정 경계 데이터 임포트 도구

한국의 **행정동**, **시군구**, **시도** 경계 데이터를 PostGIS 데이터베이스에 임포트하고 관리하는 통합 도구입니다.  
GeoPandas를 활용한 자동 경계 병합 및 다층 행정구역 데이터 생성을 지원합니다.

## 🏗️ 프로젝트 구조

```
postgis-sqls/
├── main.py                           # 메인 실행 파일
├── pyproject.toml                    # 프로젝트 설정
├── .env                              # 환경변수 (사용자 설정)
├── data/                             # 데이터 파일
│   └── HangJeongDong_ver20250401.geojson
├── sql/                              # SQL 스크립트
│   ├── create_hangjungdong_table.sql    # 행정동 테이블
│   ├── create_sigungu_table.sql         # 시군구 테이블  
│   ├── create_sido_boundary_table.sql   # 시도 테이블
│   └── postgis_korea_epsg_towgs84.sql   # 한국 좌표계
└── src/                              # 소스 코드
    ├── sqlClient/                    # 데이터베이스 연결 패키지
    │   ├── __init__.py
    │   ├── config.py                 # 환경변수 및 DB 설정
    │   ├── database.py               # DB 클라이언트 (psycopg2 래퍼)
    │   └── exceptions.py             # 예외 클래스
    └── boundaryImporter/             # GeoJSON 처리 패키지
        ├── __init__.py
        ├── geojson_loader.py         # GeoJSON 파일 로더
        ├── data_processor.py         # 데이터 변환/검증
        ├── importer.py               # 메인 임포트 로직
        └── exceptions.py             # 예외 클래스
```

## 🚀 빠른 시작

### 1. 환경 설정

```bash
# UV 사용 (권장) - 모든 의존성 자동 설치
uv sync

# 또는 개별 패키지 설치
uv add psycopg2-binary python-dotenv geopandas shapely

# pip 사용하는 경우
pip install psycopg2-binary python-dotenv geopandas shapely
```

### 2. 환경변수 설정

`.env` 파일을 생성하고 데이터베이스 정보를 입력:

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=korea_gis
DB_USER=postgres
DB_PASSWORD=your_password
```

### 3. 사용법

#### 🏗️ 데이터베이스 테이블 생성

```bash
# 기본 - 행정동 테이블만 생성
uv run python main.py setup

# 특정 테이블 생성
uv run python main.py setup --table hangjungdong  # 행정동
uv run python main.py setup --table sigungu       # 시군구 
uv run python main.py setup --table sido          # 시도

# 모든 테이블 한번에 생성
uv run python main.py setup --table all

# 시도 경계 자동 생성 (행정동 데이터 필요)
uv run python main.py setup --table sido --create-sido-from-data
```

#### 📥 데이터 임포트 및 분석

```bash
# 데이터 분석 (DB 연결 없이)
uv run python main.py analyze

# 행정동 데이터 임포트
uv run python main.py import

# 데이터 검증
uv run python main.py verify
```

### 4. 고급 사용법

```bash
# 커스텀 SQL 파일로 테이블 생성
uv run python main.py setup --sql-file custom_table.sql

# 커스텀 파일로 임포트
uv run python main.py import --geojson-file custom_data.geojson

# 기존 데이터 유지하고 추가
uv run python main.py import --append

# 커스텀 테이블명 사용
uv run python main.py import --table-name my_boundaries

# 도움말 보기
uv run python main.py --help
uv run python main.py setup --help
```

## 🗺️ 지원하는 데이터 계층

### 1. 행정동 (3,554개)
- **상세도**: 최고 (동/읍/면 단위)
- **용도**: 배달, 부동산, 상권분석, 정확한 위치 서비스
- **테이블**: `hangjungdong`

### 2. 시군구 (252개) 
- **상세도**: 중간 (시/군/구 단위)
- **용도**: 교통계획, 물류, 학군, 중간 규모 분석
- **테이블**: `sigungu` 
- **생성방법**: 행정동 데이터 자동 병합

### 3. 시도 (17개)
- **상세도**: 광역 (시/도 단위) 
- **용도**: 기상정보, 광역 정책, 전국 단위 분석
- **테이블**: `sido_boundary`
- **생성방법**: 행정동 데이터 자동 병합 + 통계

## ⚡ GeoPandas 기반 자동 병합

### 병합 성능 비교
| 방법 | 파일크기 | 처리속도 | 데이터품질 |
|------|----------|----------|------------|
| **Shapely** | 30MB (10% 감소) | 30초 | 기본 |
| **GeoPandas** | **13MB (60% 감소)** | **0.9초** | **UTM-K 면적계산** |

### 자동 생성 프로세스
1. **행정동 데이터 로드** → 3,554개 동
2. **시도별 그룹화** → 17개 시도  
3. **GeoPandas dissolve** → 공간 병합
4. **PostGIS 저장** → WKT → ST_GeomFromText
5. **통계 자동 계산** → 면적, 시군구수, 동수

## 📦 패키지 설명

### sqlClient 패키지
- **config.py**: dotenv를 사용한 환경변수 관리
- **database.py**: psycopg2 래퍼 클래스로 DB 연결/SQL 실행
- **exceptions.py**: 데이터베이스 관련 예외 클래스

주요 기능:
- 환경변수 기반 DB 설정
- SQL 파일 실행
- 트랜잭션 관리
- 연결 상태 관리

### boundaryImporter 패키지
- **geojson_loader.py**: 다양한 인코딩 지원 GeoJSON 로더
- **data_processor.py**: 데이터 변환 및 검증
- **importer.py**: 메인 임포트 로직
- **exceptions.py**: 임포트 관련 예외 클래스

주요 기능:
- GeoJSON 파일 파싱 및 검증
- 배치 처리
- 진행률 표시
- 오류 처리 및 통계

## 📊 데이터 정보

- **총 행정동 수**: 3,554개
- **시도 개수**: 17개
- **시군구 개수**: 230개
- **파일 크기**: 33.0 MB
- **좌표계**: WGS84 (EPSG:4326)
- **기하 타입**: MultiPolygon

### 상위 시도별 행정동 수
1. 경기도: 601개
2. 서울특별시: 426개
3. 경상북도: 322개
4. 경상남도: 305개
5. 전라남도: 297개

## 🛠️ 개발 정보

### 주요 기술 스택
- **Python 3.8+**
- **PostgreSQL + PostGIS**
- **psycopg2-binary**: PostgreSQL 연결
- **python-dotenv**: 환경변수 관리
- **GeoPandas**: 공간 데이터 처리 및 병합
- **Shapely**: 기하학적 연산
- **UV**: 패키지 관리 (권장)

### 데이터베이스 스키마

#### 행정동 테이블 (hangjungdong)
```sql
CREATE TABLE hangjungdong (
    id SERIAL PRIMARY KEY,
    adm_nm VARCHAR(100) NOT NULL,        -- 행정구역명
    adm_cd VARCHAR(20),                  -- 행정구역코드
    sido VARCHAR(10),                    -- 시도코드
    sgg VARCHAR(10),                     -- 시군구코드
    sidonm VARCHAR(50),                  -- 시도명
    sggnm VARCHAR(50),                   -- 시군구명
    geom GEOMETRY(MULTIPOLYGON, 4326),   -- 경계 기하정보
    created_at TIMESTAMP DEFAULT NOW()
);
```

#### 시군구 테이블 (sigungu) 
```sql
CREATE TABLE sigungu (
    id SERIAL PRIMARY KEY,
    sido VARCHAR(10),                    -- 시도코드
    sgg VARCHAR(10),                     -- 시군구코드  
    sidonm VARCHAR(50) NOT NULL,         -- 시도명
    sggnm VARCHAR(50) NOT NULL,          -- 시군구명
    geom GEOMETRY(MULTIPOLYGON, 4326),   -- 병합된 경계
    created_at TIMESTAMP DEFAULT NOW()
);
```

#### 시도 테이블 (sido_boundary)
```sql
CREATE TABLE sido_boundary (
    id SERIAL PRIMARY KEY,
    sido_code VARCHAR(10) NOT NULL,      -- 시도코드
    sido_name VARCHAR(50) NOT NULL,      -- 시도명
    geom GEOMETRY(MULTIPOLYGON, 4326),   -- 경계 기하정보
    created_at TIMESTAMP DEFAULT NOW()
);
```

> **💡 면적과 개수 정보는 실시간 계산**  
> 동 개수, 시군구 개수, 면적 등은 테이블에 저장하지 않고 SQL 쿼리로 실시간 계산합니다.  
> 이렇게 하면 데이터 일관성이 보장되고 저장 공간도 절약됩니다.

### 성능 최적화
- 배치 처리 (기본 100개씩)
- 공간 인덱스 (GIST)
- 트랜잭션 관리
- 진행률 표시

## 🔍 사용 예시

### 전체 워크플로우
```bash
# 1. 데이터 분석
uv run python main.py analyze

# 2. 모든 테이블 생성
uv run python main.py setup --table all

# 3. 행정동 데이터 임포트  
uv run python main.py import

# 4. 시도 경계 자동 생성
uv run python main.py setup --table sido --create-sido-from-data

# 5. 결과 검증
uv run python main.py verify
```

### 단계별 워크플로우
```bash
# Step 1: 행정동 테이블 + 데이터
uv run python main.py setup --table hangjungdong
uv run python main.py import

# Step 2: 시군구 경계 생성 (GeoPandas)
# (별도 스크립트나 수동 병합)

# Step 3: 시도 경계 자동 생성  
uv run python main.py setup --table sido --create-sido-from-data
```

### 공간 쿼리 예시

#### 다층 행정구역 조회
```sql
-- 특정 점의 모든 행정 계층 조회
WITH point_location AS (
    SELECT ST_SetSRID(ST_Point(126.9779, 37.5663), 4326) as pt
)
SELECT 
    h.adm_nm as 행정동,
    s.sggnm as 시군구, 
    sb.sido_name as 시도
FROM hangjungdong h
JOIN sigungu s ON h.sidonm = s.sidonm AND h.sggnm = s.sggnm  
JOIN sido_boundary sb ON h.sidonm = sb.sido_name
CROSS JOIN point_location p
WHERE ST_Contains(h.geom, p.pt);

-- 시도별 통계 (실시간 계산)
SELECT 
    sb.sido_name,
    COUNT(h.id) as dong_count,
    COUNT(DISTINCT h.sggnm) as sigungu_count,
    ROUND(CAST(ST_Area(ST_Transform(sb.geom, 5179)) / 1000000 AS NUMERIC), 2) as area_km2
FROM sido_boundary sb
LEFT JOIN hangjungdong h ON h.sidonm = sb.sido_name
GROUP BY sb.sido_name, sb.geom
ORDER BY dong_count DESC;

-- 시군구별 면적 순위 (실시간 계산)
SELECT 
    s.sidonm || ' ' || s.sggnm as 시군구,
    COUNT(h.id) as 동수,
    ROUND(CAST(ST_Area(ST_Transform(s.geom, 5179)) / 1000000 AS NUMERIC), 2) as 면적_km2,
    RANK() OVER (ORDER BY ST_Area(ST_Transform(s.geom, 5179)) DESC) as 면적순위
FROM sigungu s
LEFT JOIN hangjungdong h ON h.sidonm = s.sidonm AND h.sggnm = s.sggnm
GROUP BY s.sidonm, s.sggnm, s.geom
HAVING ST_Area(ST_Transform(s.geom, 5179)) / 1000000 > 100  -- 100km² 이상
ORDER BY 면적_km2 DESC;
```

## 🔄 지속적 업데이트 워크플로우

행정구역 데이터가 변경될 때 자동으로 모든 계층을 업데이트:

```bash
# 1. 새로운 행정동 GeoJSON 파일로 교체
# data/HangJeongDong_ver20250401.geojson → 새 파일

# 2. 행정동 데이터 업데이트
uv run python main.py import

# 3. 상위 계층 자동 재생성  
uv run python main.py setup --table sido --create-sido-from-data

# 4. 검증
uv run python main.py verify
```

### 🎯 핵심 장점

✅ **통합 관리**: 행정동 → 시군구 → 시도 전체 계층  
✅ **자동 병합**: GeoPandas 기반 빠르고 정확한 공간 연산  
✅ **성능 최적화**: 파일크기 60% 절약, 처리속도 30배 향상  
✅ **확장성**: 새로운 데이터 소스 쉽게 추가 가능  
✅ **검증**: 데이터 품질 자동 검사 및 통계  

## 📝 라이선스

MIT 라이선스

## 🤝 기여

버그 리포트나 기능 개선 제안을 환영합니다!  
특히 다음 영역에서의 기여를 기다립니다:

- 추가 행정구역 데이터 소스 지원
- 다른 국가 행정구역 데이터 확장  
- 성능 최적화 및 메모리 효율성 개선
- 웹 API 서버 기능 추가