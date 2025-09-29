package com.aiva.community.domain.post.service

import com.aiva.community.domain.post.dto.CreatePostRequest
import com.aiva.community.domain.post.dto.CommunityPostWithAuthor
import com.aiva.community.domain.post.entity.CommunityPost
import com.aiva.community.domain.post.repository.CommunityPostRepository
import com.aiva.common.redis.service.RedisCommunityServiceV2
import com.aiva.community.global.cache.toCommunityPostCache
import com.aiva.community.domain.user.UserGrpcClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class CommunityPostCreateService(
    private val communityPostRepository: CommunityPostRepository,
    private val communityPostImageService: CommunityPostImageService,
    private val redisCommunityServiceV2: RedisCommunityServiceV2,
    private val userGrpcClient: UserGrpcClient
) {

    fun createPost(userId: UUID, nickname: String, profileUrl: String?, request: CreatePostRequest): UUID {
        val post = communityPostRepository.save(
            CommunityPost(
                userId = userId,
                title = request.title,
                content = request.content
            )
        )
        communityPostImageService.createAll(post.id, request.imageUrls)

        // 게시물 캐싱: Hash + SortedSet 구조로 저장
        val postCache = post.toCommunityPostCache()
        redisCommunityServiceV2.savePost(postCache)
        
        // 작성자 정보 캐싱: user:{authorId}:profile
        redisCommunityServiceV2.cacheUserProfile(userId, nickname, profileUrl)

        return post.id
    }
    
    /**
     * 호환성을 위한 오버로드 메서드
     */
    fun createPost(userId: UUID, request: CreatePostRequest): UUID {
        return createPost(userId, "Unknown", null, request)
    }
    
    /**
     * 백그라운드에서 최신순 페이지 2-5 무효화 (트래픽이 적은 시간대에 실행)
     * 실제 서비스에서는 @Async나 메시지 큐를 활용
     */
    fun invalidateOtherPagesAsync() {
        // TODO: 최신순 p2~p5 무효화 구현 필요 (새 글은 보통 상단만 영향)
        // 현재는 Redis SortedSet 구조로 페이징이 실시간 반영되므로 별도 무효화 불필요
    }
}