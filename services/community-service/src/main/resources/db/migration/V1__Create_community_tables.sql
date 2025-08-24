-- 커뮤니티 게시글 테이블 생성
CREATE TABLE community_posts (
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    content VARCHAR(1000) NOT NULL,
    like_count INT NOT NULL DEFAULT 0,
    comment_count INT NOT NULL DEFAULT 0,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_community_posts_user_id (user_id),
    INDEX idx_community_posts_created_at (created_at),
    INDEX idx_community_posts_like_count (like_count)
);

-- 게시글 이미지 테이블 생성
CREATE TABLE community_post_images (
    id BINARY(16) PRIMARY KEY,
    post_id BINARY(16) NOT NULL,
    url TEXT NOT NULL,
    width INT NULL,
    height INT NULL,
    mime_type VARCHAR(50) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY fk_post_images_post_id (post_id) REFERENCES community_posts(id) ON DELETE CASCADE,
    INDEX idx_post_images_post_id (post_id)
);

-- 게시글 좋아요 테이블 생성
CREATE TABLE community_likes (
    id BINARY(16) PRIMARY KEY,
    post_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_community_likes_post_user (post_id, user_id),
    FOREIGN KEY fk_community_likes_post_id (post_id) REFERENCES community_posts(id) ON DELETE CASCADE,
    INDEX idx_community_likes_user_id (user_id)
);

-- 댓글 테이블 생성
CREATE TABLE comments (
    id BINARY(16) PRIMARY KEY,
    post_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    parent_comment_id BINARY(16) NULL,
    content VARCHAR(300) NOT NULL,
    like_count INT NOT NULL DEFAULT 0,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY fk_comments_post_id (post_id) REFERENCES community_posts(id) ON DELETE CASCADE,
    FOREIGN KEY fk_comments_parent_id (parent_comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    INDEX idx_comments_post_id (post_id),
    INDEX idx_comments_user_id (user_id),
    INDEX idx_comments_parent_id (parent_comment_id),
    INDEX idx_comments_created_at (created_at)
);

-- 댓글 좋아요 테이블 생성
CREATE TABLE comment_likes (
    id BINARY(16) PRIMARY KEY,
    comment_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_comment_likes_comment_user (comment_id, user_id),
    FOREIGN KEY fk_comment_likes_comment_id (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    INDEX idx_comment_likes_user_id (user_id)
);

-- 신고 테이블 생성
CREATE TABLE reports (
    id BINARY(16) PRIMARY KEY,
    reporter_user_id BINARY(16) NOT NULL,
    target_type ENUM('POST', 'COMMENT') NOT NULL,
    target_id BINARY(16) NOT NULL,
    reason_code VARCHAR(30) NOT NULL,
    details TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_reports_reporter_user_id (reporter_user_id),
    INDEX idx_reports_target (target_type, target_id),
    INDEX idx_reports_created_at (created_at)
);
