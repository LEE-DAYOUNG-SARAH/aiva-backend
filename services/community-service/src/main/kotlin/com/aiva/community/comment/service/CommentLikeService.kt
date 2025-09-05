package com.aiva.community.comment.service

import com.aiva.community.comment.entity.CommentLike
import com.aiva.community.comment.repository.CommentLikeRepository
import com.aiva.community.post.dto.LikeResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class CommentLikeService(
    private val commentLikeRepository: CommentLikeRepository,
    private val commentService: CommentService
) {

    @Transactional
    fun likeComment(commentId: UUID, userId: UUID): LikeResponse {
        // Verify comment exists and is active
        commentService.getActiveCommentById(commentId)
        
        val existingLike = commentLikeRepository.findByCommentIdAndUserId(commentId, userId)
        
        if (existingLike.isPresent) {
            return LikeResponse(isLiked = true, likeCount = commentLikeRepository.countByCommentId(commentId))
        }
        
        val like = CommentLike(
            commentId = commentId,
            userId = userId
        )
        
        commentLikeRepository.save(like)
        commentService.incrementLikeCount(commentId)
        
        return LikeResponse(isLiked = true, likeCount = commentLikeRepository.countByCommentId(commentId))
    }

    @Transactional
    fun unlikeComment(commentId: UUID, userId: UUID): LikeResponse {
        // Verify comment exists and is active
        commentService.getActiveCommentById(commentId)
        
        val existingLike = commentLikeRepository.findByCommentIdAndUserId(commentId, userId)
        
        if (existingLike.isEmpty) {
            return LikeResponse(isLiked = false, likeCount = commentLikeRepository.countByCommentId(commentId))
        }
        
        commentLikeRepository.deleteByCommentIdAndUserId(commentId, userId)
        commentService.decrementLikeCount(commentId)
        
        return LikeResponse(isLiked = false, likeCount = commentLikeRepository.countByCommentId(commentId))
    }

    @Transactional
    fun toggleCommentLike(commentId: UUID, userId: UUID): LikeResponse {
        val isLiked = isCommentLikedByUser(commentId, userId)
        return if (isLiked) {
            unlikeComment(commentId, userId)
        } else {
            likeComment(commentId, userId)
        }
    }

    fun isCommentLikedByUser(commentId: UUID, userId: UUID): Boolean {
        return commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)
    }

    fun getCommentLikeCount(commentId: UUID): Long {
        return commentLikeRepository.countByCommentId(commentId)
    }
}