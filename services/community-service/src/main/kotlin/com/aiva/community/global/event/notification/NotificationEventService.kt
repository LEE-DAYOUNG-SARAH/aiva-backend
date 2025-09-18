package com.aiva.community.global.event.notification

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.util.*

/**
 * 알림 이벤트 발행을 위한 헬퍼 서비스
 * 
 * 비즈니스 로직에서 쉽게 알림 이벤트를 발행할 수 있도록 도우는 서비스입니다.
 * Spring의 ApplicationEventPublisher를 통해 이벤트를 발행하여 트랜잭션 안전성을 보장합니다.
 */
@Service
class NotificationEventService(
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    
    private val logger = LoggerFactory.getLogger(NotificationEventService::class.java)
    
    /**
     * 게시글 좋아요 알림 발행
     */
    fun publishPostLikedNotification(
        postAuthorId: UUID,
        likerUserId: UUID,
        postId: UUID,
        postTitle: String,
        likerNickname: String
    ) {
        // 본인이 본인 게시글에 좋아요 누른 경우 알림 발송하지 않음
        if (postAuthorId == likerUserId) {
            logger.debug("Skipping self-like notification for user: $likerUserId")
            return
        }
        
        val event = NotificationEventFactory.createPostLikedEvent(
            postAuthorId = postAuthorId,
            likerUserId = likerUserId,
            postId = postId,
            postTitle = postTitle,
            likerNickname = likerNickname
        )
        
        publishEvent(event)
        logger.debug("Published post liked notification: postId=$postId, liker=$likerUserId")
    }
    
    /**
     * 댓글 작성 알림 발행
     */
    fun publishCommentCreatedNotification(
        postAuthorId: UUID,
        commenterUserId: UUID,
        postId: UUID,
        commentId: UUID,
        postTitle: String,
        commenterNickname: String,
        commentContent: String
    ) {
        // 본인이 본인 게시글에 댓글 단 경우 알림 발송하지 않음
        if (postAuthorId == commenterUserId) {
            logger.debug("Skipping self-comment notification for user: $commenterUserId")
            return
        }
        
        val event = NotificationEventFactory.createCommentCreatedEvent(
            postAuthorId = postAuthorId,
            commenterUserId = commenterUserId,
            postId = postId,
            commentId = commentId,
            postTitle = postTitle,
            commenterNickname = commenterNickname,
            commentContent = commentContent
        )
        
        publishEvent(event)
        logger.debug("Published comment created notification: postId=$postId, commentId=$commentId")
    }
    
    /**
     * 대댓글 작성 알림 발행
     */
    fun publishReplyCreatedNotification(
        originalCommenterUserId: UUID,
        replierUserId: UUID,
        postId: UUID,
        originalCommentId: UUID,
        replyCommentId: UUID,
        replierNickname: String,
        replyContent: String
    ) {
        // 본인이 본인 댓글에 답글 단 경우 알림 발송하지 않음
        if (originalCommenterUserId == replierUserId) {
            logger.debug("Skipping self-reply notification for user: $replierUserId")
            return
        }
        
        val event = NotificationEventFactory.createReplyCreatedEvent(
            originalCommenterUserId = originalCommenterUserId,
            replierUserId = replierUserId,
            postId = postId,
            originalCommentId = originalCommentId,
            replyCommentId = replyCommentId,
            replierNickname = replierNickname,
            replyContent = replyContent
        )
        
        publishEvent(event)
        logger.debug("Published reply created notification: originalCommentId=$originalCommentId, replyId=$replyCommentId")
    }
    
    /**
     * 댓글 좋아요 알림 발행
     */
    fun publishCommentLikedNotification(
        commentAuthorId: UUID,
        likerUserId: UUID,
        commentId: UUID,
        postId: UUID,
        likerNickname: String
    ) {
        // 본인이 본인 댓글에 좋아요 누른 경우 알림 발송하지 않음
        if (commentAuthorId == likerUserId) {
            logger.debug("Skipping self-comment-like notification for user: $likerUserId")
            return
        }
        
        val event = NotificationEventFactory.createCommentLikedEvent(
            commentAuthorId = commentAuthorId,
            likerUserId = likerUserId,
            commentId = commentId,
            postId = postId,
            likerNickname = likerNickname
        )
        
        publishEvent(event)
        logger.debug("Published comment liked notification: commentId=$commentId, liker=$likerUserId")
    }
    
    /**
     * Spring 이벤트 발행
     * 트랜잭션 커밋 후에 Kafka로 전송되도록 함
     */
    private fun publishEvent(event: NotificationEvent) {
        try {
            applicationEventPublisher.publishEvent(NotificationApplicationEvent(event))
        } catch (e: Exception) {
            logger.error("Failed to publish application event for notification: ${event.eventId}", e)
            // 알림 발송 실패가 비즈니스 로직을 방해하지 않도록 예외를 삼킴
        }
    }
}