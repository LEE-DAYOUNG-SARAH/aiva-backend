package com.aiva.community.domain.comment.service

import com.aiva.community.domain.comment.dto.UpdateCommentRequest
import com.aiva.community.domain.post.service.CommunityPostUpdateService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class CommentUpdateService(
    private val communityPostUpdateService: CommunityPostUpdateService,
    private val commentReadService: CommentReadService
) {

    fun updateComment(commentId: UUID,userId: UUID, request: UpdateCommentRequest): UUID {
        val comment = commentReadService.getActiveCommentById(commentId)
        require(comment.userId == userId) { "User can only update their own comments" }
        
        comment.update(request.content)
        
        return comment.id
    }

    fun deleteComment(commentId: UUID, userId: UUID): Unit {
        val comment = commentReadService.getActiveCommentById(commentId)
        require(comment.userId == userId) { "User can only delete their own comments" }
        
        comment.delete()

        communityPostUpdateService.decrementCommentCount(comment.postId)
    }

}