-- 시도 경계 데이터를 위한 PostGIS 테이블 생성 스크립트
-- 작성일: 2025년
-- 설명: 한국 시도 경계 데이터 저장을 위한 테이블

-- PostGIS 확장 기능이 설치되어 있는지 확인
CREATE EXTENSION IF NOT EXISTS postgis;

-- 기존 테이블이 있다면 삭제 (주의: 기존 데이터가 삭제됩니다)
DROP TABLE IF EXISTS sido_boundary CASCADE;

CREATE TABLE sido_boundary (
    id SERIAL PRIMARY KEY,                    -- 자동증가 기본키
    sido_code VARCHAR(10) NOT NULL,           -- 시도코드
    sido_name VARCHAR(50) NOT NULL,           -- 시도명
    geom GEOMETRY(MULTIPOLYGON, 4326),        -- 경계 기하정보 (WGS84)
    created_at TIMESTAMP DEFAULT NOW(),       -- 생성일시
    updated_at TIMESTAMP DEFAULT NOW()        -- 수정일시
);

-- 공간 인덱스 생성 (검색 성능 향상)
CREATE INDEX idx_sido_boundary_geom ON sido_boundary USING GIST (geom);

-- 시도코드 인덱스 생성
CREATE INDEX idx_sido_boundary_code ON sido_boundary (sido_code);

-- 시도명 인덱스 생성
CREATE INDEX idx_sido_boundary_name ON sido_boundary (sido_name);



-- 유니크 인덱스 (시도코드)
CREATE UNIQUE INDEX idx_sido_boundary_code_unique ON sido_boundary (sido_code);

-- 테이블에 코멘트 추가
COMMENT ON TABLE sido_boundary IS '한국 시도 경계 데이터';
COMMENT ON COLUMN sido_boundary.id IS '기본키';
COMMENT ON COLUMN sido_boundary.sido_code IS '시도코드';
COMMENT ON COLUMN sido_boundary.sido_name IS '시도명';
COMMENT ON COLUMN sido_boundary.geom IS '경계 기하정보 (WGS84 좌표계)';

-- 테이블 생성 완료 메시지
SELECT 'sido_boundary 테이블이 성공적으로 생성되었습니다.' as message;

-- 좌표계 정보 확인
SELECT 
    f_table_name,
    f_geometry_column,
    coord_dimension,
    srid,
    type
FROM geometry_columns 
WHERE f_table_name = 'sido_boundary';
