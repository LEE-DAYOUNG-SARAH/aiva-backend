-- 구독 플랜 테이블 생성
CREATE TABLE subscription_plans (
    id BINARY(16) PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    billing_period_unit ENUM('MONTH', 'YEAR') NOT NULL,
    billing_period_count INT NOT NULL,
    price_amount INT NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'KRW',
    trial_days INT NOT NULL DEFAULT 0,
    usage_limit_per_period INT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_subscription_plans_code (code),
    INDEX idx_subscription_plans_is_active (is_active)
);

-- 사용자 구독 테이블 생성
CREATE TABLE user_subscriptions (
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL UNIQUE,
    plan_id BINARY(16) NOT NULL,
    status ENUM('TRIALING', 'ACTIVE', 'PAST_DUE', 'CANCELED', 'EXPIRED', 'PAUSED') NOT NULL,
    auto_renew BOOLEAN NOT NULL DEFAULT TRUE,
    started_at TIMESTAMP NOT NULL,
    current_period_start TIMESTAMP NOT NULL,
    current_period_end TIMESTAMP NOT NULL,
    next_billing_at TIMESTAMP NULL,
    cancel_at_period_end BOOLEAN NOT NULL DEFAULT FALSE,
    canceled_at TIMESTAMP NULL,
    origin_order_id BINARY(16) NULL,
    latest_order_id BINARY(16) NULL,
    usage_count INT NOT NULL DEFAULT 0,
    usage_limit_per_period INT NULL,
    usage_period_start TIMESTAMP NOT NULL,
    usage_period_end TIMESTAMP NOT NULL,
    last_usage_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY fk_user_subscriptions_plan_id (plan_id) REFERENCES subscription_plans(id),
    INDEX idx_user_subscriptions_user_id (user_id),
    INDEX idx_user_subscriptions_status (status),
    INDEX idx_user_subscriptions_next_billing_at (next_billing_at)
);

-- 구독 주문 테이블 생성
CREATE TABLE subscription_orders (
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    plan_id BINARY(16) NOT NULL,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    order_type ENUM('NEW', 'RENEWAL', 'UPGRADE', 'DOWNGRADE') NOT NULL,
    amount INT NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'KRW',
    status ENUM('PENDING', 'COMPLETED', 'CANCELED', 'FAILED') NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP NULL,
    canceled_at TIMESTAMP NULL,
    failure_reason VARCHAR(300) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY fk_subscription_orders_plan_id (plan_id) REFERENCES subscription_plans(id),
    INDEX idx_subscription_orders_user_id (user_id),
    INDEX idx_subscription_orders_order_no (order_no),
    INDEX idx_subscription_orders_status (status),
    INDEX idx_subscription_orders_created_at (created_at)
);

-- 결제 테이블 생성
CREATE TABLE payments (
    id BINARY(16) PRIMARY KEY,
    order_id BINARY(16) NOT NULL,
    provider ENUM('TOSS_PAYMENTS', 'IAMPORT', 'APPLE', 'GOOGLE') NOT NULL,
    method ENUM('CARD', 'BANK', 'IAP') NOT NULL,
    external_id VARCHAR(100) NULL,
    status ENUM('PENDING', 'AUTHORIZED', 'PAID', 'FAILED', 'REFUNDED', 'PARTIAL_REFUNDED') NOT NULL,
    amount INT NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'KRW',
    approved_at TIMESTAMP NULL,
    failed_at TIMESTAMP NULL,
    refunded_at TIMESTAMP NULL,
    card_last4 VARCHAR(4) NULL,
    receipt_url VARCHAR(300) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY fk_payments_order_id (order_id) REFERENCES subscription_orders(id) ON DELETE CASCADE,
    INDEX idx_payments_order_id (order_id),
    INDEX idx_payments_external_id (external_id),
    INDEX idx_payments_status (status),
    INDEX idx_payments_created_at (created_at)
);

-- 구독 취소 테이블 생성
CREATE TABLE subscription_cancellations (
    id BINARY(16) PRIMARY KEY,
    user_subscription_id BINARY(16) NOT NULL,
    canceled_at TIMESTAMP NOT NULL,
    effective_at TIMESTAMP NOT NULL,
    initiator ENUM('USER', 'ADMIN', 'SYSTEM') NOT NULL,
    reason_code VARCHAR(30) NULL,
    reason_text VARCHAR(500) NULL,
    refund_payment_id BINARY(16) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY fk_subscription_cancellations_subscription_id (user_subscription_id) REFERENCES user_subscriptions(id) ON DELETE CASCADE,
    FOREIGN KEY fk_subscription_cancellations_refund_payment_id (refund_payment_id) REFERENCES payments(id),
    INDEX idx_subscription_cancellations_subscription_id (user_subscription_id),
    INDEX idx_subscription_cancellations_canceled_at (canceled_at)
);

-- 기본 구독 플랜 데이터 삽입
INSERT INTO subscription_plans (id, code, name, description, billing_period_unit, billing_period_count, price_amount, trial_days, usage_limit_per_period) VALUES
(UUID_TO_BIN(UUID()), 'PRO_MONTHLY', 'AIVA Pro 월간', 'AI 육아 상담 무제한 이용', 'MONTH', 1, 9900, 7, NULL),
(UUID_TO_BIN(UUID()), 'PRO_YEARLY', 'AIVA Pro 연간', 'AI 육아 상담 무제한 이용 (연간 할인)', 'YEAR', 1, 99000, 7, NULL),
(UUID_TO_BIN(UUID()), 'BASIC_MONTHLY', 'AIVA Basic 월간', 'AI 육아 상담 월 50회 이용', 'MONTH', 1, 4900, 7, 50);
