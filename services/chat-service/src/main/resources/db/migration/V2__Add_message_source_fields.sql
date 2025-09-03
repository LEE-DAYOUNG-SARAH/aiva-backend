-- 메시지 테이블에 출처 정보 컬럼 추가
ALTER TABLE messages 
ADD COLUMN source_docs TEXT NULL COMMENT 'AI 응답의 참조 문서 출처',
ADD COLUMN summary TEXT NULL COMMENT 'AI 응답 요약 정보';

-- 출처 정보 인덱스 추가 (검색 최적화)
CREATE INDEX idx_messages_source_docs ON messages(source_docs(255));