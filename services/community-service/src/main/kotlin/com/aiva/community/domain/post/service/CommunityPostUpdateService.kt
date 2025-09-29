package com.aiva.community.domain.post.service

import com.aiva.community.domain.post.dto.UpdatePostRequest
import com.aiva.common.redis.service.RedisCommunityServiceV2
import com.aiva.community.global.cache.toCommunityPostCache
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class CommunityPostUpdateService(
    private val communityPostImageService: CommunityPostImageService,
    private val communityPostReadService: CommunityPostReadService,
    private val redisCommunityServiceV2: RedisCommunityServiceV2,
) {

    fun updatePost(postId: UUID, userId: UUID, request: UpdatePostRequest): UUID {
        val postWithAuthor = communityPostReadService.getActivePostWithAuthor(postId)
        require(postWithAuthor.post.userId == userId) { "User can only update their own posts" }
        
        postWithAuthor.post.update(request.title, request.content)

        communityPostImageService.update(postId, request.images)
        
        // 캐시 전략: 게시물 수정 시 Redis Hash 업데이트
        val updatedPostCache = postWithAuthor.post.toCommunityPostCache()
        redisCommunityServiceV2.savePost(updatedPostCache)
        
        return postId
    }

    fun deletePost(postId: UUID, userId: UUID): UUID {
        val postWithAuthor = communityPostReadService.getActivePostWithAuthor(postId)
        require(postWithAuthor.post.userId == userId) { "User can only delete their own posts" }

        postWithAuthor.post.delete()
        
        // 캐시 전략: 게시물 삭제 시 Redis에서 제거
        redisCommunityServiceV2.deletePost(postId)

        return postWithAuthor.post.id
    }

    fun incrementLikeCount(postId: UUID) {
        val post = communityPostReadService.getActivePostById(postId)
        post.incrementLikeCount()
        
        // Redis에서 좋아요 수 증가
        redisCommunityServiceV2.incrementLikeCount(postId)
    }

    fun decrementLikeCount(postId: UUID) {
        val post = communityPostReadService.getActivePostById(postId)
        post.decrementLikeCount()
        
        // Redis에서 좋아요 수 감소
        redisCommunityServiceV2.decrementLikeCount(postId)
    }

    fun incrementCommentCount(postId: UUID) {
        val post = communityPostReadService.getActivePostById(postId)
        post.incrementCommentCount()
        
        // Redis Hash에서 댓글 수 업데이트 (직접 Hash 필드 증가는 RedisCommunityServiceV2에 메서드 추가 필요)
        val updatedPostCache = post.toCommunityPostCache()
        redisCommunityServiceV2.savePost(updatedPostCache)
    }

    fun decrementCommentCount(postId: UUID) {
        val post = communityPostReadService.getActivePostById(postId)
        post.decrementCommentCount()
        
        // Redis Hash에서 댓글 수 업데이트
        val updatedPostCache = post.toCommunityPostCache()
        redisCommunityServiceV2.savePost(updatedPostCache)
    }
}