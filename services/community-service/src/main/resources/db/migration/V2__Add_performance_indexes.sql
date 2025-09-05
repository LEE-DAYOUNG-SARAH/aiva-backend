-- 성능 최적화를 위한 복합 인덱스 추가

-- 게시글 피드 조회 최적화 (삭제되지 않은 게시글, 생성일자 내림차순)
CREATE INDEX idx_community_posts_active_created_desc 
ON community_posts (deleted_at, created_at DESC) 
WHERE deleted_at IS NULL;

-- 사용자별 게시글 조회 최적화
CREATE INDEX idx_community_posts_user_active_created_desc 
ON community_posts (user_id, deleted_at, created_at DESC) 
WHERE deleted_at IS NULL;

-- 인기 게시글 조회 최적화 (좋아요 수, 댓글 수 기준)
CREATE INDEX idx_community_posts_popularity 
ON community_posts (like_count DESC, comment_count DESC, created_at DESC) 
WHERE deleted_at IS NULL;

-- 댓글 조회 최적화 (게시글별, 최상위 댓글, 생성일자 순)
CREATE INDEX idx_comments_post_toplevel_created 
ON comments (post_id, parent_comment_id, created_at ASC) 
WHERE deleted_at IS NULL;

-- 대댓글 조회 최적화
CREATE INDEX idx_comments_parent_active_created 
ON comments (parent_comment_id, deleted_at, created_at ASC) 
WHERE deleted_at IS NULL AND parent_comment_id IS NOT NULL;

-- 사용자별 댓글 조회 최적화
CREATE INDEX idx_comments_user_active_created_desc 
ON comments (user_id, deleted_at, created_at DESC) 
WHERE deleted_at IS NULL;

-- 신고 관리 최적화 (타입별, 생성일자)
CREATE INDEX idx_reports_target_type_created_desc 
ON reports (target_type, created_at DESC);

-- 중복 신고 체크 최적화
CREATE INDEX idx_reports_duplicate_check 
ON reports (reporter_user_id, target_type, target_id);

-- 특정 컨텐츠의 신고 수 조회 최적화
CREATE INDEX idx_reports_target_content 
ON reports (target_type, target_id, created_at DESC);