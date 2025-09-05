package com.aiva.community.post.service

import com.aiva.community.post.dto.UpdatePostRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class CommunityPostUpdateService(
    private val communityPostImageService: CommunityPostImageService,
    private val communityPostReadService: CommunityPostReadService
) {

    fun updatePost(postId: UUID, userId: UUID, request: UpdatePostRequest): UUID {
        val post = communityPostReadService.getActivePostById(postId)
        require(post.userId == userId) { "User can only update their own posts" }
        
        post.update(request.title, request.content)

        communityPostImageService.update(postId, request.images)
        
        return postId
    }

    fun deletePost(postId: UUID, userId: UUID):UUID {
        val post = communityPostReadService.getActivePostById(postId)
        require(post.userId == userId) { "User can only delete their own posts" }

        post.delete()

        return post.id
    }

    fun incrementLikeCount(postId: UUID) {
        val post = communityPostReadService.getActivePostById(postId)
        post.incrementLikeCount()
    }

    fun decrementLikeCount(postId: UUID) {
        val post = communityPostReadService.getActivePostById(postId)
        post.decrementLikeCount()
    }

    fun incrementCommentCount(postId: UUID) {
        val post = communityPostReadService.getActivePostById(postId)
        post.incrementCommentCount()
    }

    fun decrementCommentCount(postId: UUID) {
        val post = communityPostReadService.getActivePostById(postId)
        post.decrementCommentCount()
    }
}