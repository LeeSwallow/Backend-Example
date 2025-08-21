-- 행정동 경계 데이터를 위한 PostGIS 테이블 생성 스크립트
-- 작성일: 2025년
-- 설명: 한국 행정동 경계 데이터 저장을 위한 테이블

-- PostGIS 확장 기능이 설치되어 있는지 확인
CREATE EXTENSION IF NOT EXISTS postgis;

-- 기존 테이블이 있다면 삭제 (주의: 기존 데이터가 삭제됩니다)
DROP TABLE IF EXISTS hangjungdong CASCADE;

-- 행정동 경계 테이블 생성
CREATE TABLE hangjungdong (
    id SERIAL PRIMARY KEY,                    -- 자동증가 기본키
    adm_nm VARCHAR(100) NOT NULL,             -- 행정구역명 (예: 서울특별시 종로구 사직동)
    adm_cd VARCHAR(20),                       -- 행정구역코드
    adm_cd2 VARCHAR(20),                      -- 행정구역코드2
    sido VARCHAR(10),                         -- 시도코드
    sgg VARCHAR(10),                          -- 시군구코드
    sidonm VARCHAR(50),                       -- 시도명
    sggnm VARCHAR(50),                        -- 시군구명
    geom GEOMETRY(MULTIPOLYGON, 4326),        -- 경계 기하정보 (WGS84)
    created_at TIMESTAMP DEFAULT NOW(),       -- 생성일시
    updated_at TIMESTAMP DEFAULT NOW()        -- 수정일시
);

-- 공간 인덱스 생성 (검색 성능 향상)
CREATE INDEX idx_hangjungdong_geom ON hangjungdong USING GIST (geom);

-- 행정구역명 인덱스 생성
CREATE INDEX idx_hangjungdong_adm_nm ON hangjungdong (adm_nm);

-- 행정구역코드 인덱스 생성
CREATE INDEX idx_hangjungdong_adm_cd ON hangjungdong (adm_cd);
CREATE INDEX idx_hangjungdong_adm_cd2 ON hangjungdong (adm_cd2);

-- 시도, 시군구 인덱스 생성
CREATE INDEX idx_hangjungdong_sido ON hangjungdong (sido);
CREATE INDEX idx_hangjungdong_sgg ON hangjungdong (sgg);

-- 테이블에 코멘트 추가
COMMENT ON TABLE hangjungdong IS '한국 행정동 경계 데이터';
COMMENT ON COLUMN hangjungdong.id IS '기본키';
COMMENT ON COLUMN hangjungdong.adm_nm IS '행정구역명';
COMMENT ON COLUMN hangjungdong.adm_cd IS '행정구역코드';
COMMENT ON COLUMN hangjungdong.adm_cd2 IS '행정구역코드2';
COMMENT ON COLUMN hangjungdong.sido IS '시도코드';
COMMENT ON COLUMN hangjungdong.sgg IS '시군구코드';
COMMENT ON COLUMN hangjungdong.sidonm IS '시도명';
COMMENT ON COLUMN hangjungdong.sggnm IS '시군구명';
COMMENT ON COLUMN hangjungdong.geom IS '경계 기하정보 (WGS84 좌표계)';

-- 테이블 생성 완료 메시지
SELECT 'hangjungdong 테이블이 성공적으로 생성되었습니다.' as message;

-- 좌표계 정보 확인
SELECT 
    f_table_name,
    f_geometry_column,
    coord_dimension,
    srid,
    type
FROM geometry_columns 
WHERE f_table_name = 'hangjungdong';