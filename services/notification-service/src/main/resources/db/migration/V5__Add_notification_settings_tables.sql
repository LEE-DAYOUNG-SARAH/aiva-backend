-- 알림 설정 테이블 추가

-- user_notification_permissions 테이블 생성
CREATE TABLE user_notification_permissions (
    id BINARY(16) NOT NULL PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    permission_type ENUM('POLICY_INFO', 'COMMUNITY', 'BILLING', 'MARKETING') NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_notification_permissions_user_id (user_id),
    INDEX idx_user_notification_permissions_permission_type (permission_type),
    INDEX idx_user_notification_permissions_enabled (is_enabled),
    UNIQUE KEY uk_user_permission (user_id, permission_type)
);

-- notification_consent_events 테이블 생성
CREATE TABLE notification_consent_events (
    id BINARY(16) NOT NULL PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    permission_type ENUM('POLICY_INFO', 'COMMUNITY', 'BILLING', 'MARKETING') NOT NULL,
    action ENUM('OPT_IN', 'OPT_OUT') NOT NULL,
    source ENUM('SYSTEM', 'MYPAGE_TOGGLE', 'ADMIN_CONSOLE', 'MIGRATION') NOT NULL,
    policy_version VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX ix_nce_user_cat_created (user_id, permission_type, created_at DESC),
    INDEX ix_nce_user_created (user_id, created_at DESC),
    INDEX idx_consent_events_permission_type (permission_type),
    INDEX idx_consent_events_action (action)
);