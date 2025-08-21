-- Pin 테이블 생성 SQL
-- PostGIS 확장이 필요합니다: CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE pin (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    location GEOMETRY(POINT, 4326),
    sigungu_id INTEGER,                             -- 시군구 참조 외래키
    CONSTRAINT fk_pin_sigungu FOREIGN KEY (sigungu_id) REFERENCES sigungu(id)
);

-- 공간 인덱스 생성 (성능 향상을 위해)
CREATE INDEX idx_pin_location ON pin USING GIST (location);

-- 외래키 인덱스 생성 (조인 성능 향상을 위해)
CREATE INDEX idx_pin_sigungu_id ON pin (sigungu_id);

-- 테이블 설명 추가
COMMENT ON TABLE pin IS '핀 정보 테이블';
COMMENT ON COLUMN pin.id IS '핀 고유 식별자 (UUID)';
COMMENT ON COLUMN pin.name IS '핀 이름';
COMMENT ON COLUMN pin.location IS '핀 위치 (WGS84 좌표계, SRID 4326)';
COMMENT ON COLUMN pin.sigungu_id IS '소속 시군구 ID (sigungu 테이블 참조)';
