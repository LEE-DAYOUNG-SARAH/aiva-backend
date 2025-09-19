package com.aiva.community.global.event.topic

/**
 * Kafka 토픽 상수 관리
 * 
 * 모든 토픽명을 중앙집중식으로 관리하여 일관성을 보장합니다.
 */
object KafkaTopics {
    
    // 알림 관련 토픽
    const val COMMUNITY_NOTIFICATION = "community.notification"
    
    // 사용자 프로필 관련 토픽
    const val USER_PROFILE_CHANGED = "user.profile.changed"
    
    // 커뮤니티 이벤트 관련 토픽
    const val COMMUNITY_POST_CREATED = "community.post.created"
    const val COMMUNITY_POST_UPDATED = "community.post.updated"
    const val COMMUNITY_POST_DELETED = "community.post.deleted"
    
    // 댓글 관련 토픽
    const val COMMUNITY_COMMENT_CREATED = "community.comment.created"
    const val COMMUNITY_COMMENT_DELETED = "community.comment.deleted"
}

/**
 * Consumer Group 상수 관리
 */
object ConsumerGroups {
    const val COMMUNITY_SERVICE = "community-service-group"
    const val NOTIFICATION_SERVICE = "notification-service-group"
    const val USER_SERVICE = "user-service-group"
}