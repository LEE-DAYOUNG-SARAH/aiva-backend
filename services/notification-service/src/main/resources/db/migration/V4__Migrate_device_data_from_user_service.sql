-- user-service에서 notification-service로 디바이스 데이터 마이그레이션
-- 실제 운영 환경에서는 다음 단계로 실행:
-- 1. user_service DB에서 데이터 추출
-- 2. notification_service DB로 데이터 삽입
-- 3. 데이터 정합성 검증

-- 이 스크립트는 예시이며, 실제로는 별도의 마이그레이션 도구나 스크립트 사용 권장

-- 예시: 다른 데이터베이스에서 데이터를 가져오는 경우
-- INSERT INTO user_devices (id, user_id, device_identifier, platform, device_model, os_version, app_version, last_seen_at, deleted_at, created_at, updated_at)
-- SELECT id, user_id, device_identifier, platform, device_model, os_version, app_version, last_seen_at, deleted_at, created_at, updated_at
-- FROM external_db.user_devices;

-- INSERT INTO fcm_tokens (id, user_device_id, fcm_token, last_validated_at, is_active, created_at, updated_at)  
-- SELECT id, user_device_id, fcm_token, last_validated_at, is_active, created_at, updated_at
-- FROM external_db.fcm_tokens;

-- 마이그레이션 완료 표시
-- CREATE TABLE migration_status (
--     table_name VARCHAR(50) PRIMARY KEY,
--     migrated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     record_count INT,
--     status ENUM('COMPLETED', 'FAILED') DEFAULT 'COMPLETED'
-- );

-- INSERT INTO migration_status (table_name, record_count) VALUES 
-- ('user_devices', (SELECT COUNT(*) FROM user_devices)),
-- ('fcm_tokens', (SELECT COUNT(*) FROM fcm_tokens));

-- 임시로 빈 마이그레이션 (실제 데이터는 별도 도구로 이관)
SELECT 'Device data migration placeholder - use external migration tool' as migration_note;