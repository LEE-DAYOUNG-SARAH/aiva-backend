package com.aiva.community.domain.comment.service

import com.aiva.community.domain.comment.dto.CreateCommentRequest
import com.aiva.community.domain.comment.entity.Comment
import com.aiva.community.domain.comment.repository.CommentRepository
import com.aiva.community.domain.post.service.CommunityPostReadService
import com.aiva.community.domain.post.service.CommunityPostUpdateService
import com.aiva.community.domain.user.UserGrpcClient
import com.aiva.community.global.event.notification.NotificationEventService
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class CommentCreateService(
    private val commentRepository: CommentRepository,
    private val communityPostUpdateService: CommunityPostUpdateService,
    private val communityPostReadService: CommunityPostReadService,
    private val commentReadService: CommentReadService,
    private val userGrpcClient: UserGrpcClient,
    private val notificationEventService: NotificationEventService
) {
    val log = KotlinLogging.logger {  }

    fun createComment(postId: UUID, userId: UUID, request: CreateCommentRequest): UUID {
        val post = communityPostReadService.getActivePostById(postId)
        
        val comment = commentRepository.save(
            Comment(
                postId = postId,
                userId = userId,
                content = request.content.trim()
            )
        )

        communityPostUpdateService.incrementCommentCount(postId)
        
        // 댓글 작성 알림 이벤트 발행
        try {
            val commenterProfile = userGrpcClient.getUserProfile(userId)
            if (commenterProfile != null) {
                notificationEventService.publishCommentCreatedNotification(
                    postAuthorId = post.userId,
                    commenterUserId = userId,
                    postId = postId,
                    commentId = comment.id,
                    postTitle = post.title,
                    commenterNickname = commenterProfile.nickname,
                    commentContent = comment.content
                )
                log.debug { "Published comment created notification: postId=$postId, commentId=${comment.id}" }
            } else {
                log.warn { "User profile not found via gRPC for comment notification: userId=$userId" }
            }
        } catch (e: Exception) {
            log.error(e) { "Failed to publish comment created notification: postId=$postId, commentId=${comment.id}" }
        }
        
        return comment.id
    }

    fun createReply(postId: UUID, parentCommentId: UUID, userId: UUID, request: CreateCommentRequest): UUID {
        val parentComment = commentReadService.getActiveCommentById(parentCommentId)
        require(parentComment.postId == postId) { "Parent comment must belong to the same post" }
        require(parentComment.parentCommentId == null) { "Cannot reply to a reply. Only top-level comments can have replies" }

        val reply = commentRepository.save(
            Comment(
                postId = postId,
                userId = userId,
                parentCommentId = parentCommentId,
                content = request.content.trim()
            )
        )

        communityPostUpdateService.incrementCommentCount(postId)
        
        // 대댓글 작성 알림 이벤트 발행
        try {
            val replierProfile = userGrpcClient.getUserProfile(userId)
            if (replierProfile != null) {
                notificationEventService.publishReplyCreatedNotification(
                    originalCommenterUserId = parentComment.userId,
                    replierUserId = userId,
                    postId = postId,
                    originalCommentId = parentCommentId,
                    replyCommentId = reply.id,
                    replierNickname = replierProfile.nickname,
                    replyContent = reply.content
                )
                log.debug { "Published reply created notification: parentCommentId=$parentCommentId, replyId=${reply.id}" }
            } else {
                log.warn { "User profile not found via gRPC for reply notification: userId=$userId" }
            }
        } catch (e: Exception) {
            log.error(e) { "Failed to publish reply created notification: parentCommentId=$parentCommentId, replyId=${reply.id}" }
        }
        
        return reply.id
    }
}