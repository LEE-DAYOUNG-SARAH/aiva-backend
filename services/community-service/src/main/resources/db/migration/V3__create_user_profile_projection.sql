-- User Profile Projection for Community Service
-- 이벤트 기반으로 User 서비스로부터 동기화되는 프로필 정보

CREATE TABLE IF NOT EXISTS user_profile_projection (
    user_id     BINARY(16) PRIMARY KEY COMMENT 'UUID from user service',
    nickname    VARCHAR(50) NOT NULL COMMENT 'User display name',
    avatar_url  TEXT COMMENT 'Profile image URL',
    level       INT NOT NULL DEFAULT 0 COMMENT 'User level or tier',
    version     BIGINT NOT NULL DEFAULT 0 COMMENT 'Version for optimistic locking and ordering',
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'First creation timestamp'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User profile projection for community service';

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_user_profile_updated_at ON user_profile_projection(updated_at);
CREATE INDEX IF NOT EXISTS idx_user_profile_nickname ON user_profile_projection(nickname);
CREATE INDEX IF NOT EXISTS idx_user_profile_level ON user_profile_projection(level);

-- 샘플 데이터 (개발/테스트용)
-- 실제 프로덕션에서는 이벤트를 통해서만 데이터가 들어옴
INSERT INTO user_profile_projection (user_id, nickname, avatar_url, level, version, updated_at) 
VALUES 
    (UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440000', '-', '')), 'TestUser1', 'https://example.com/avatar1.jpg', 1, 1, NOW()),
    (UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440001', '-', '')), 'TestUser2', 'https://example.com/avatar2.jpg', 2, 1, NOW()),
    (UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440002', '-', '')), 'TestUser3', NULL, 0, 1, NOW())
ON DUPLICATE KEY UPDATE 
    nickname = VALUES(nickname),
    avatar_url = VALUES(avatar_url),
    level = VALUES(level),
    version = VALUES(version),
    updated_at = VALUES(updated_at);