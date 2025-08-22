DROP TABLE IF EXISTS user_auth_provider CASCADE;
DROP TABLE IF EXISTS user_role CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS providers CASCADE;
DROP TABLE IF EXISTS refresh_tokens CASCADE;

CREATE TABLE providers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT GEN_RANDOM_UUID(),
    email TEXT UNIQUE,
    password TEXT,  -- 외부 인증만 사용할 땐 NULL 가능
    age INT CHECK (age >= 0),
    name VARCHAR(100) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    account_non_expired BOOLEAN DEFAULT TRUE,
    credentials_non_expired BOOLEAN DEFAULT TRUE,
    failed_longin_attempts INT DEFAULT 0,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_auth_provider (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider_id INT NOT NULL REFERENCES providers(id),
    client_id VARCHAR(255) NOT NULL, -- 외부 인증에서 부여하는 사용자 고유 ID
    PRIMARY KEY (user_id, provider_id),
    UNIQUE (provider_id, client_id) -- provider_id와 clientId의 조합은 유일해야 함
);

CREATE TABLE user_role (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id INT NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

-- 임시로 db에 저장되는 토큰 테이블
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT GEN_RANDOM_UUID(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token TEXT NOT NULL UNIQUE,
    expired_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL
)