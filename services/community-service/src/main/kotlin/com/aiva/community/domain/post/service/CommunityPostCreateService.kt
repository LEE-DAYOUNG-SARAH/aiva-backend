package com.aiva.community.domain.post.service

import com.aiva.community.domain.post.dto.CreatePostRequest
import com.aiva.community.domain.post.dto.CommunityPostWithAuthor
import com.aiva.community.domain.post.entity.CommunityPost
import com.aiva.community.domain.post.repository.CommunityPostRepository
import com.aiva.community.global.cache.CommunityPostCacheService
import com.aiva.community.domain.user.AuthorInfo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class CommunityPostCreateService(
    private val communityPostRepository: CommunityPostRepository,
    private val communityPostImageService: CommunityPostImageService,
    private val cacheService: CommunityPostCacheService,
    private val communityPostUserService: CommunityPostUserService
) {

    fun createPost(userId: UUID, request: CreatePostRequest): UUID {
        val post = communityPostRepository.save(
            CommunityPost(
                userId = userId,
                title = request.title,
                content = request.content
            )
        )
        communityPostImageService.createAll(post.id, request.imageUrls)

        // 캐시 전략: 새 글 생성 시
        // 1. 작성자 정보와 함께 캐시에 저장 (최신글이므로 조회 가능성 높음)
        val authorInfo = communityPostUserService.getUserProfile(userId)
            ?: communityPostUserService.createFallbackAuthorInfo(userId)

        cacheService.cachePost(
            CommunityPostWithAuthor(
                post = post,
                imageUrls = request.imageUrls,
                author = authorInfo
            )
        )

        // 2. 최신순 목록 무효화
        cacheService.evictLatestPostPages(startPage = 1, endPage = 1)

        // 3. 최신순 버전 증가로 무효화 감지
        cacheService.incrementLatestListVersion()

        return post.id
    }
    
    /**
     * 백그라운드에서 최신순 페이지 2-5 무효화 (트래픽이 적은 시간대에 실행)
     * 실제 서비스에서는 @Async나 메시지 큐를 활용
     */
    fun invalidateOtherPagesAsync() {
        // 최신순 p2~p5 무효화 (새 글은 보통 상단만 영향)
        cacheService.evictLatestPostPages(startPage = 2, endPage = 5)
    }
}