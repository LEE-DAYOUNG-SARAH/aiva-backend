package com.aiva.community.domain.comment.service

import com.aiva.community.domain.comment.dto.CreateCommentRequest
import com.aiva.community.domain.comment.entity.Comment
import com.aiva.community.domain.comment.repository.CommentRepository
import com.aiva.community.domain.post.service.CommunityPostReadService
import com.aiva.community.domain.post.service.CommunityPostUpdateService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class CommentCreateService(
    private val commentRepository: CommentRepository,
    private val communityPostUpdateService: CommunityPostUpdateService,
    private val communityPostReadService: CommunityPostReadService,
    private val commentReadService: CommentReadService
) {

    fun createComment(postId: UUID, userId: UUID, request: CreateCommentRequest): UUID {
        communityPostReadService.getActivePostById(postId)
        
        val comment = commentRepository.save(
            Comment(
                postId = postId,
                userId = userId,
                content = request.content.trim()
            )
        )

        communityPostUpdateService.incrementCommentCount(postId)
        
        return comment.id
    }

    fun createReply(postId: UUID, parentCommentId: UUID, userId: UUID, request: CreateCommentRequest): UUID {
        communityPostReadService.getActivePostById(postId)
        
        val parentComment = commentReadService.getActiveCommentById(parentCommentId)
        require(parentComment.postId == postId) { "Parent comment must belong to the same post" }
        require(parentComment.parentCommentId == null) { "Cannot reply to a reply. Only top-level comments can have replies" }

        val replay = commentRepository.save(
            Comment(
                postId = postId,
                userId = userId,
                parentCommentId = parentCommentId,
                content = request.content.trim()
            )
        )

        communityPostUpdateService.incrementCommentCount(postId)
        
        return replay.id
    }
}