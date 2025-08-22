-- Insert Providers
INSERT INTO providers (name) VALUES
    ('KAKAO'),
    ('GOOGLE');

-- Insert Roles
INSERT INTO roles (name) VALUES
    ('ROLE_GUEST'),
    ('ROLE_USER'),
    ('ROLE_ADMIN');

WITH inserted_admin AS (
    INSERT INTO users (email, password, age, name)VALUES (
        'admin@example.com',
        '$2a$10$9Y3Yute3c7RixQyn3Qc0le.zJMNz8TibiQX1nKRVNTyIXsXTXjXhO',
        30,
        '관리자'
     ) RETURNING id
),
role_admin AS (
    SELECT id FROM roles WHERE name = 'ROLE_ADMIN'
)
INSERT INTO user_role (user_id, role_id)
SELECT (SELECT id FROM inserted_admin), id FROM role_admin;