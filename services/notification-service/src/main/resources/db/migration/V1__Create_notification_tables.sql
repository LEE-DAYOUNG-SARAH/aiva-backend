-- 알림 테이블 생성
CREATE TABLE notifications (
    id BINARY(16) PRIMARY KEY,
    type ENUM('COMMUNITY_COMMENT', 'ANNOUNCEMENT', 'MARKETING', 'POLICY_INFO', 'EVENTS') NOT NULL,
    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    image_url TEXT NULL,
    link_url TEXT NULL,
    scheduled_at TIMESTAMP NULL,
    created_by BINARY(16) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_notifications_type (type),
    INDEX idx_notifications_scheduled_at (scheduled_at),
    INDEX idx_notifications_created_at (created_at)
);

-- 알림 수신자 테이블 생성
CREATE TABLE notification_recipients (
    id BINARY(16) PRIMARY KEY,
    notification_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_notification_recipients (notification_id, user_id),
    FOREIGN KEY fk_notification_recipients_notification_id (notification_id) REFERENCES notifications(id) ON DELETE CASCADE,
    INDEX idx_notification_recipients_user_id (user_id),
    INDEX idx_notification_recipients_is_read (is_read)
);

-- 알림 작업 테이블 생성 (Outbox + 전송상태 통합)
CREATE TABLE notification_jobs (
    id BINARY(16) PRIMARY KEY,
    notification_recipient_id BINARY(16) NOT NULL,
    fcm_token_id BINARY(16) NOT NULL,
    payload JSON NOT NULL,
    enqueue_status ENUM('PENDING', 'ENQUEUED', 'SKIPPED', 'FAILED') NOT NULL DEFAULT 'PENDING',
    delivery_status ENUM('QUEUED', 'SENT', 'DELIVERED', 'FAILED') NOT NULL DEFAULT 'QUEUED',
    attempt_count INT NOT NULL DEFAULT 0,
    sns_message_id VARCHAR(100) NULL,
    provider_message_id VARCHAR(100) NULL,
    enqueued_at TIMESTAMP NULL,
    first_sent_at TIMESTAMP NULL,
    last_sent_at TIMESTAMP NULL,
    delivered_at TIMESTAMP NULL,
    last_error_code VARCHAR(50) NULL,
    last_error_message TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_notification_jobs (notification_recipient_id, fcm_token_id),
    FOREIGN KEY fk_notification_jobs_recipient_id (notification_recipient_id) REFERENCES notification_recipients(id) ON DELETE CASCADE,
    INDEX idx_notification_jobs_enqueue_status (enqueue_status),
    INDEX idx_notification_jobs_delivery_status (delivery_status),
    INDEX idx_notification_jobs_created_at (created_at)
);

-- 사용자 알림 설정 테이블 생성
CREATE TABLE user_notification_settings (
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL UNIQUE,
    policy_info_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    community_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    events_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    marketing_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_notification_settings_user_id (user_id)
);

-- 알림 동의 이벤트 테이블 생성 (법적 증빙용)
CREATE TABLE notification_consent_events (
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    category_key VARCHAR(50) NOT NULL,
    action ENUM('OPT_IN', 'OPT_OUT') NOT NULL,
    source ENUM('SIGNUP', 'MYPAGE_TOGGLE', 'ADMIN_CONSOLE', 'MIGRATION') NOT NULL,
    policy_version VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_notification_consent_events_user_id (user_id),
    INDEX idx_notification_consent_events_category (category_key),
    INDEX idx_notification_consent_events_created_at (created_at)
);
