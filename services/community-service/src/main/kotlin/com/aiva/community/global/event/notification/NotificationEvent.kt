package com.aiva.community.global.event.notification

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.util.*

/**
 * 커뮤니티에서 발생하는 알림 이벤트
 * 
 * 알림 대상:
 * 1. 게시글 좋아요 - 게시글 작성자에게 알림
 * 2. 댓글 작성 - 게시글 작성자에게 알림
 * 3. 대댓글 작성 - 원본 댓글 작성자에게 알림
 * 4. 댓글 좋아요 - 댓글 작성자에게 알림
 */
data class NotificationEvent(
    @JsonProperty("eventId")
    val eventId: String,
    
    @JsonProperty("eventType")
    val eventType: NotificationEventType,
    
    @JsonProperty("targetUserId")
    val targetUserId: UUID, // 알림 받을 사용자 ID
    
    @JsonProperty("actorUserId")
    val actorUserId: UUID, // 행동을 한 사용자 ID
    
    @JsonProperty("resourceType")
    val resourceType: ResourceType,
    
    @JsonProperty("resourceId")
    val resourceId: UUID, // 게시글 ID 또는 댓글 ID
    
    @JsonProperty("parentResourceId")
    val parentResourceId: UUID? = null, // 댓글의 경우 게시글 ID, 대댓글의 경우 원본 댓글 ID
    
    @JsonProperty("title")
    val title: String, // 알림 제목
    
    @JsonProperty("content")
    val content: String, // 알림 내용
    
    @JsonProperty("metadata")
    val metadata: Map<String, Any> = emptyMap(), // 추가 메타데이터
    
    @JsonProperty("createdAt")
    val createdAt: Instant = Instant.now()
) {
    
    companion object {
        const val TOPIC_NAME = "community.notification"
        
        /**
         * 멱등키 생성 (자연키 형태)
         * 형식: {eventType}:{targetUserId}:{actorUserId}:{resourceId}[:{parentResourceId}]
         */
        fun generateIdempotentKey(
            eventType: NotificationEventType,
            targetUserId: UUID,
            actorUserId: UUID,
            resourceId: UUID,
            parentResourceId: UUID? = null
        ): String {
            return if (parentResourceId != null) {
                "${eventType.name}:${targetUserId}:${actorUserId}:${resourceId}:${parentResourceId}"
            } else {
                "${eventType.name}:${targetUserId}:${actorUserId}:${resourceId}"
            }
        }
    }
}

/**
 * 알림 이벤트 타입
 */
enum class NotificationEventType {
    @JsonProperty("post_liked")
    POST_LIKED,
    
    @JsonProperty("comment_created")
    COMMENT_CREATED,
    
    @JsonProperty("reply_created")
    REPLY_CREATED,
    
    @JsonProperty("comment_liked")
    COMMENT_LIKED
}

/**
 * 리소스 타입
 */
enum class ResourceType {
    @JsonProperty("post")
    POST,
    
    @JsonProperty("comment")
    COMMENT
}

/**
 * 알림 이벤트 빌더
 */
class NotificationEventBuilder {
    private var eventType: NotificationEventType? = null
    private var targetUserId: UUID? = null
    private var actorUserId: UUID? = null
    private var resourceType: ResourceType? = null
    private var resourceId: UUID? = null
    private var parentResourceId: UUID? = null
    private var title: String = ""
    private var content: String = ""
    private var metadata: Map<String, Any> = emptyMap()
    
    fun eventType(eventType: NotificationEventType) = apply { this.eventType = eventType }
    fun targetUserId(targetUserId: UUID) = apply { this.targetUserId = targetUserId }
    fun actorUserId(actorUserId: UUID) = apply { this.actorUserId = actorUserId }
    fun resourceType(resourceType: ResourceType) = apply { this.resourceType = resourceType }
    fun resourceId(resourceId: UUID) = apply { this.resourceId = resourceId }
    fun parentResourceId(parentResourceId: UUID?) = apply { this.parentResourceId = parentResourceId }
    fun title(title: String) = apply { this.title = title }
    fun content(content: String) = apply { this.content = content }
    fun metadata(metadata: Map<String, Any>) = apply { this.metadata = metadata }
    
    fun build(): NotificationEvent {
        requireNotNull(eventType) { "eventType is required" }
        requireNotNull(targetUserId) { "targetUserId is required" }
        requireNotNull(actorUserId) { "actorUserId is required" }
        requireNotNull(resourceType) { "resourceType is required" }
        requireNotNull(resourceId) { "resourceId is required" }
        require(title.isNotBlank()) { "title cannot be blank" }
        require(content.isNotBlank()) { "content cannot be blank" }
        
        // 멱등키 자동 생성
        val idempotentKey = NotificationEvent.generateIdempotentKey(
            eventType = eventType!!,
            targetUserId = targetUserId!!,
            actorUserId = actorUserId!!,
            resourceId = resourceId!!,
            parentResourceId = parentResourceId
        )
        
        return NotificationEvent(
            eventId = idempotentKey,
            eventType = eventType!!,
            targetUserId = targetUserId!!,
            actorUserId = actorUserId!!,
            resourceType = resourceType!!,
            resourceId = resourceId!!,
            parentResourceId = parentResourceId,
            title = title,
            content = content,
            metadata = metadata
        )
    }
}

/**
 * 편의 메서드들
 */
object NotificationEventFactory {
    
    /**
     * 게시글 좋아요 이벤트 생성
     */
    fun createPostLikedEvent(
        postAuthorId: UUID,
        likerUserId: UUID,
        postId: UUID,
        postTitle: String,
        likerNickname: String
    ): NotificationEvent {
        return NotificationEventBuilder()
            .eventType(NotificationEventType.POST_LIKED)
            .targetUserId(postAuthorId)
            .actorUserId(likerUserId)
            .resourceType(ResourceType.POST)
            .resourceId(postId)
            .title("게시글에 좋아요가 눌렸어요")
            .content("${likerNickname}님이 '${postTitle}' 게시글을 좋아합니다")
            .metadata(mapOf(
                "postTitle" to postTitle,
                "likerNickname" to likerNickname
            ))
            .build()
    }
    
    /**
     * 댓글 작성 이벤트 생성
     */
    fun createCommentCreatedEvent(
        postAuthorId: UUID,
        commenterUserId: UUID,
        postId: UUID,
        commentId: UUID,
        postTitle: String,
        commenterNickname: String,
        commentContent: String
    ): NotificationEvent {
        return NotificationEventBuilder()
            .eventType(NotificationEventType.COMMENT_CREATED)
            .targetUserId(postAuthorId)
            .actorUserId(commenterUserId)
            .resourceType(ResourceType.COMMENT)
            .resourceId(commentId)
            .parentResourceId(postId)
            .title("게시글에 댓글이 달렸어요")
            .content("${commenterNickname}님이 '${postTitle}' 게시글에 댓글을 남겼습니다")
            .metadata(mapOf(
                "postTitle" to postTitle,
                "commenterNickname" to commenterNickname,
                "commentContent" to commentContent.take(50) + if (commentContent.length > 50) "..." else ""
            ))
            .build()
    }
    
    /**
     * 대댓글 작성 이벤트 생성
     */
    fun createReplyCreatedEvent(
        originalCommenterUserId: UUID,
        replierUserId: UUID,
        postId: UUID,
        originalCommentId: UUID,
        replyCommentId: UUID,
        replierNickname: String,
        replyContent: String
    ): NotificationEvent {
        return NotificationEventBuilder()
            .eventType(NotificationEventType.REPLY_CREATED)
            .targetUserId(originalCommenterUserId)
            .actorUserId(replierUserId)
            .resourceType(ResourceType.COMMENT)
            .resourceId(replyCommentId)
            .parentResourceId(originalCommentId)
            .title("댓글에 답글이 달렸어요")
            .content("${replierNickname}님이 회원님의 댓글에 답글을 남겼습니다")
            .metadata(mapOf(
                "postId" to postId.toString(),
                "replierNickname" to replierNickname,
                "replyContent" to replyContent.take(50) + if (replyContent.length > 50) "..." else ""
            ))
            .build()
    }
    
    /**
     * 댓글 좋아요 이벤트 생성
     */
    fun createCommentLikedEvent(
        commentAuthorId: UUID,
        likerUserId: UUID,
        commentId: UUID,
        postId: UUID,
        likerNickname: String
    ): NotificationEvent {
        return NotificationEventBuilder()
            .eventType(NotificationEventType.COMMENT_LIKED)
            .targetUserId(commentAuthorId)
            .actorUserId(likerUserId)
            .resourceType(ResourceType.COMMENT)
            .resourceId(commentId)
            .parentResourceId(postId)
            .title("댓글에 좋아요가 눌렸어요")
            .content("${likerNickname}님이 회원님의 댓글을 좋아합니다")
            .metadata(mapOf(
                "postId" to postId.toString(),
                "likerNickname" to likerNickname
            ))
            .build()
    }
}