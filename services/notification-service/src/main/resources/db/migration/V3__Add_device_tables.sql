-- 디바이스 관리 테이블 추가

-- user_devices 테이블 생성
CREATE TABLE user_devices (
    id BINARY(16) NOT NULL PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    device_identifier VARCHAR(255) NOT NULL,
    platform ENUM('ANDROID', 'IOS', 'WEB') NOT NULL,
    device_model VARCHAR(100),
    os_version VARCHAR(50),
    app_version VARCHAR(50) NOT NULL,
    last_seen_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_devices_user_id (user_id),
    INDEX idx_user_devices_device_identifier (device_identifier),
    INDEX idx_user_devices_last_seen (last_seen_at),
    UNIQUE KEY uk_user_device (user_id, device_identifier, deleted_at)
);

-- fcm_tokens 테이블 생성  
CREATE TABLE fcm_tokens (
    id BINARY(16) NOT NULL PRIMARY KEY,
    user_device_id BINARY(16) NOT NULL,
    fcm_token TEXT NOT NULL,
    last_validated_at TIMESTAMP NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_fcm_tokens_user_device_id (user_device_id),
    INDEX idx_fcm_tokens_is_active (is_active),
    INDEX idx_fcm_tokens_last_validated (last_validated_at),
    UNIQUE KEY uk_fcm_token (fcm_token(255)),
    
    FOREIGN KEY (user_device_id) REFERENCES user_devices(id) ON DELETE CASCADE
);