-- 알림 테이블에 읽음 상태 컬럼 추가
-- NotificationRecipient는 유지하고 Notification에만 isRead, readAt 추가

ALTER TABLE notifications 
ADD COLUMN is_read BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN read_at TIMESTAMP NULL;

-- 인덱스 추가
CREATE INDEX idx_notifications_is_read ON notifications(is_read);

-- 기존 NotificationRecipient 데이터를 기반으로 읽음 상태 업데이트
UPDATE notifications n
JOIN notification_recipients nr ON n.id = nr.notification_id
SET n.is_read = nr.is_read, n.read_at = nr.read_at;