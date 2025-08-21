-- 시군구 경계 데이터를 위한 PostGIS 테이블 생성 스크립트
-- 작성일: 2025년
-- 설명: 한국 시군구 경계 데이터 저장을 위한 테이블

-- PostGIS 확장 기능이 설치되어 있는지 확인
CREATE EXTENSION IF NOT EXISTS postgis;

-- 기존 테이블이 있다면 삭제 (주의: 기존 데이터가 삭제됩니다)
DROP TABLE IF EXISTS sigungu CASCADE;

-- 시군구 경계 테이블 생성
CREATE TABLE sigungu (
    id SERIAL PRIMARY KEY,                    -- 자동증가 기본키
    sido VARCHAR(10),                         -- 시도코드
    sgg VARCHAR(10),                          -- 시군구코드
    sidonm VARCHAR(50) NOT NULL,              -- 시도명
    sggnm VARCHAR(50) NOT NULL,               -- 시군구명
    geom GEOMETRY(MULTIPOLYGON, 4326),        -- 병합된 경계 기하정보 (WGS84)
    created_at TIMESTAMP DEFAULT NOW(),       -- 생성일시
    updated_at TIMESTAMP DEFAULT NOW()        -- 수정일시
);

-- 공간 인덱스 생성 (검색 성능 향상)
CREATE INDEX idx_sigungu_geom ON sigungu USING GIST (geom);

-- 시도명 인덱스 생성
CREATE INDEX idx_sigungu_sidonm ON sigungu (sidonm);

-- 시군구명 인덱스 생성
CREATE INDEX idx_sigungu_sggnm ON sigungu (sggnm);

-- 시도코드, 시군구코드 인덱스 생성
CREATE INDEX idx_sigungu_sido ON sigungu (sido);
CREATE INDEX idx_sigungu_sgg ON sigungu (sgg);



-- 복합 인덱스 (시도+시군구)
CREATE UNIQUE INDEX idx_sigungu_sido_sgg ON sigungu (sido, sgg);

-- 테이블에 코멘트 추가
COMMENT ON TABLE sigungu IS '한국 시군구(행정구) 경계 데이터 - 동 단위에서 집계됨';
COMMENT ON COLUMN sigungu.id IS '기본키';
COMMENT ON COLUMN sigungu.sido IS '시도코드';
COMMENT ON COLUMN sigungu.sgg IS '시군구코드';
COMMENT ON COLUMN sigungu.sidonm IS '시도명';
COMMENT ON COLUMN sigungu.sggnm IS '시군구명';
COMMENT ON COLUMN sigungu.geom IS '병합된 경계 기하정보 (WGS84 좌표계)';

-- 테이블 생성 완료 메시지
SELECT 'sigungu 테이블이 성공적으로 생성되었습니다.' as message;

-- 좌표계 정보 확인
SELECT 
    f_table_name,
    f_geometry_column,
    coord_dimension,
    srid,
    type
FROM geometry_columns 
WHERE f_table_name = 'sigungu';