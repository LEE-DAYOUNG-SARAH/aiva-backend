package com.aiva.community.domain.comment.service

import com.aiva.community.domain.comment.entity.CommentLike
import com.aiva.community.domain.comment.repository.CommentLikeRepository
import com.aiva.community.domain.post.dto.LikeResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class CommentLikeService(
    private val commentLikeRepository: CommentLikeRepository,
    private val commentReadService: CommentReadService
) {

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