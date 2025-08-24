-- 채팅 테이블 생성
CREATE TABLE chats (
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    title VARCHAR(200) NOT NULL,
    pinned BOOLEAN NOT NULL DEFAULT FALSE,
    pinned_at TIMESTAMP NULL,
    last_message_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_chats_user_id (user_id),
    INDEX idx_chats_last_message_at (last_message_at),
    INDEX idx_chats_pinned (pinned, pinned_at)
);

-- 메시지 테이블 생성
CREATE TABLE messages (
    id BINARY(16) PRIMARY KEY,
    chat_id BINARY(16) NOT NULL,
    role ENUM('USER', 'ASSISTANT') NOT NULL,
    content TEXT NOT NULL,
    stopped_by_user BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY fk_messages_chat_id (chat_id) REFERENCES chats(id) ON DELETE CASCADE,
    INDEX idx_messages_chat_id (chat_id),
    INDEX idx_messages_created_at (created_at)
);

-- FAQ 테이블 생성
CREATE TABLE chat_faqs (
    id BINARY(16) PRIMARY KEY,
    question VARCHAR(500) NOT NULL,
    answer TEXT NOT NULL,
    rank INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_chat_faqs_rank (rank)
);

-- 초기 FAQ 데이터 삽입
INSERT INTO chat_faqs (id, question, answer, rank) VALUES
(UUID_TO_BIN(UUID()), '아이가 밤에 자주 깨어요. 어떻게 해야 하나요?', '밤에 자주 깨는 것은 신생아에게 정상적인 현상입니다. 규칙적인 수면 패턴을 만들어 주세요.', 1),
(UUID_TO_BIN(UUID()), '언제부터 이유식을 시작해야 하나요?', '보통 생후 4-6개월부터 이유식을 시작합니다. 아이의 발달 상태를 보고 결정하세요.', 2),
(UUID_TO_BIN(UUID()), '아이가 열이 날 때 어떻게 대처해야 하나요?', '38도 이상의 열이 지속되면 소아과에 상담받으시고, 미지근한 물로 몸을 닦아주세요.', 3);
