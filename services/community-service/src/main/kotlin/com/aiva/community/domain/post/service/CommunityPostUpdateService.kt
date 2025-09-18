package com.aiva.community.domain.post.service

import com.aiva.community.domain.post.dto.UpdatePostRequest
import com.aiva.community.global.cache.CommunityPostCacheService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class CommunityPostUpdateService(
    private val communityPostImageService: CommunityPostImageService,
    private val communityPostReadService: CommunityPostReadService,
    private val cacheService: CommunityPostCacheService,
) {

    fun updatePost(postId: UUID, userId: UUID, request: UpdatePostRequest): UUID {
        val postWithAuthor = communityPostReadService.getActivePostWithAuthor(postId)
        require(postWithAuthor.post.userId == userId) { "User can only update their own posts" }
        
        postWithAuthor.post.update(request.title, request.content)

        communityPostImageService.update(postId, request.images)
        
        // 캐시 전략: 게시물 수정 시
        // 1. 수정된 게시물 캐시 갱신
        cacheService.cachePost(postWithAuthor)
        
        // 2. 최신순 목록만 무효화 (인기순은 배치에서 처리)
        // 수정은 좋아요/댑글수 영향 없으므로 최신순만 고려
        cacheService.evictLatestPostPages(startPage = 1, endPage = 3)
        
        return postId
    }

    fun deletePost(postId: UUID, userId: UUID): UUID {
        val postWithAuthor = communityPostReadService.getActivePostWithAuthor(postId)
        require(postWithAuthor.post.userId == userId) { "User can only delete their own posts" }

        postWithAuthor.post.delete()
        
        // 캐시 전략: 게시물 삭제 시
        // 1. 해당 게시물 캐시 삭제
        cacheService.evictPost(postId)
        
        // 2. 최신순 목록에서 제거 (인기순은 배치에서 처리)
        cacheService.evictLatestPostPages(startPage = 1, endPage = 5)
        
        // 3. 최신순 버전 증가로 무효화 감지
        cacheService.incrementLatestListVersion()

        return postWithAuthor.post.id
    }

    fun incrementLikeCount(postId: UUID) {
        val postWithAuthor = communityPostReadService.getActivePostWithAuthor(postId)
        postWithAuthor.post.incrementLikeCount()
        
        // 좋아요 수 변경 시 개별 게시물만 갱신
        cacheService.cachePost(postWithAuthor)
        
        // 인기순에는 영향을 주지만 배치에서 처리하므로 즉시 무효화하지 않음
    }

    fun decrementLikeCount(postId: UUID) {
        val postWithAuthor = communityPostReadService.getActivePostWithAuthor(postId)
        postWithAuthor.post.decrementLikeCount()
        
        // 좋아요 수 변경 시 개별 게시물만 갱신
        cacheService.cachePost(postWithAuthor)
        
        // 인기순에는 영향을 주지만 배치에서 처리하므로 즉시 무효화하지 않음
    }

    fun incrementCommentCount(postId: UUID) {
        val postWithAuthor = communityPostReadService.getActivePostWithAuthor(postId)
        postWithAuthor.post.incrementCommentCount()
        
        // 댓글 수 변경 시 개별 게시물만 갱신
        cacheService.cachePost(postWithAuthor)
        
        // 인기순에는 영향을 주지만 배치에서 처리하므로 즉시 무효화하지 않음
    }

    fun decrementCommentCount(postId: UUID) {
        val postWithAuthor = communityPostReadService.getActivePostWithAuthor(postId)
        postWithAuthor.post.decrementCommentCount()
        
        // 댓글 수 변경 시 개별 게시물만 갱신
        cacheService.cachePost(postWithAuthor)
        
        // 인기순에는 영향을 주지만 배치에서 처리하므로 즉시 무효화하지 않음
    }
}