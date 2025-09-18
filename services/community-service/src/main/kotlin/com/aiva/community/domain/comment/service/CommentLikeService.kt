package com.aiva.community.domain.comment.service

import com.aiva.community.domain.comment.entity.CommentLike
import com.aiva.community.domain.comment.repository.CommentLikeRepository
import com.aiva.community.domain.post.dto.LikeResponse
import com.aiva.community.domain.user.UserProfileProjectionRepository
import com.aiva.community.global.event.notification.NotificationEventService
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class CommentLikeService(
    private val commentLikeRepository: CommentLikeRepository,
    private val commentReadService: CommentReadService,
    private val userProfileRepository: UserProfileProjectionRepository,
    private val notificationEventService: NotificationEventService
) {
    val log = KotlinLogging.logger {  }

    @Transactional
    fun toggleCommentLike(commentId: UUID, userId: UUID): LikeResponse {
        val isLiked = isCommentLikedByUser(commentId, userId)
        return if (isLiked) {
            unlikeComment(commentId, userId)
        } else {
            likeComment(commentId, userId)
        }
    }

    private fun likeComment(commentId: UUID, userId: UUID): LikeResponse {
        // Verify comment exists and is active
        val comment = commentReadService.getActiveCommentById(commentId)
        
        val existingLike = commentLikeRepository.findByCommentIdAndUserId(commentId, userId)
        
        if (existingLike.isPresent) {
            return LikeResponse(isLiked = true, likeCount = commentLikeRepository.countByCommentId(commentId))
        }

        commentLikeRepository.save(
            CommentLike(
                commentId = commentId,
                userId = userId
            )
        )
        comment.incrementLikeCount()
        
        // 댓글 좋아요 알림 이벤트 발행
        try {
            val likerProfile = userProfileRepository.findById(userId).orElse(null)
            if (likerProfile != null) {
                notificationEventService.publishCommentLikedNotification(
                    commentAuthorId = comment.userId,
                    likerUserId = userId,
                    commentId = commentId,
                    postId = comment.postId,
                    likerNickname = likerProfile.nickname
                )
                log.debug { "Published comment liked notification: commentId=$commentId, liker=$userId" }
            } else {
                log.warn { "User profile not found for comment like notification: userId=$userId" }
            }
        } catch (e: Exception) {
            log.error(e) { "Failed to publish comment liked notification: commentId=$commentId, userId=$userId" }
        }
        
        return LikeResponse(isLiked = true, likeCount = commentLikeRepository.countByCommentId(commentId))
    }

    private fun unlikeComment(commentId: UUID, userId: UUID): LikeResponse {
        // Verify comment exists and is active
        val comment = commentReadService.getActiveCommentById(commentId)
        
        val existingLike = commentLikeRepository.findByCommentIdAndUserId(commentId, userId)
        
        if (existingLike.isEmpty) {
            return LikeResponse(isLiked = false, likeCount = commentLikeRepository.countByCommentId(commentId))
        }
        
        commentLikeRepository.deleteByCommentIdAndUserId(commentId, userId)
        comment.decrementLikeCount()
        
        return LikeResponse(isLiked = false, likeCount = commentLikeRepository.countByCommentId(commentId))
    }


    fun isCommentLikedByUser(commentId: UUID, userId: UUID): Boolean {
        return commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)
    }

    fun getCommentLikeCount(commentId: UUID): Long {
        return commentLikeRepository.countByCommentId(commentId)
    }
}