-- 사용자 테이블 생성
CREATE TABLE users (
    id BINARY(16) PRIMARY KEY,
    provider VARCHAR(20) NOT NULL,
    provider_user_id VARCHAR(128) NOT NULL,
    email VARCHAR(255) NULL,
    nickname VARCHAR(10) NOT NULL,
    avatar_url TEXT NULL,
    is_pro BOOLEAN NOT NULL DEFAULT FALSE,
    pro_expires_at TIMESTAMP NULL,
    last_login_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_users_provider_user_id (provider, provider_user_id),
    INDEX idx_users_email (email),
    INDEX idx_users_created_at (created_at)
);

-- 아이 정보 테이블 생성
CREATE TABLE children (
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    birth_type ENUM('BORN', 'DUE', 'DUE_UNKNOWN') NOT NULL,
    birth_date DATE NULL,
    gender ENUM('FEMALE', 'MALE', 'UNKNOWN') DEFAULT 'UNKNOWN',
    note VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_children_user_id (user_id),
    FOREIGN KEY fk_children_user_id (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_children_birth_date (birth_date)
);

-- 사용자 디바이스 테이블 생성
CREATE TABLE user_devices (
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    platform VARCHAR(10) NOT NULL,
    device_model VARCHAR(100) NULL,
    os_version VARCHAR(50) NULL,
    app_version VARCHAR(50) NULL,
    is_push_permitted BOOLEAN NOT NULL DEFAULT TRUE,
    last_seen_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY fk_user_devices_user_id (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_devices_user_id (user_id),
    INDEX idx_user_devices_platform (platform)
);

-- FCM 토큰 테이블 생성
CREATE TABLE fcm_tokens (
    id BINARY(16) PRIMARY KEY,
    user_device_id BINARY(16) NOT NULL,
    fcm_token TEXT NOT NULL,
    last_validated_at TIMESTAMP NULL,
    revoked_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_fcm_tokens_token (fcm_token(255)),
    FOREIGN KEY fk_fcm_tokens_device_id (user_device_id) REFERENCES user_devices(id) ON DELETE CASCADE,
    INDEX idx_fcm_tokens_device_id (user_device_id),
    INDEX idx_fcm_tokens_revoked_at (revoked_at)
);
