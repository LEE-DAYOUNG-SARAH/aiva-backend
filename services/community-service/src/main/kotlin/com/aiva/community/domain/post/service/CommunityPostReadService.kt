package com.aiva.community.domain.post.service

import com.aiva.common.response.PageResponse
import com.aiva.community.domain.post.dto.CommunityPostResponse
import com.aiva.community.domain.post.dto.CommunityPostWithAuthor
import com.aiva.community.domain.post.entity.CommunityPost
import com.aiva.community.domain.post.repository.CommunityPostRepository
import com.aiva.community.domain.post.repository.CommunityPostImageRepository
import com.aiva.community.global.cache.CommunityPostCacheService
import com.aiva.community.global.cache.toCommunityPost
import com.aiva.community.domain.post.service.CommunityPostUserService
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class CommunityPostReadService(
    private val communityPostRepository: CommunityPostRepository,
    private val communityPostImageRepository: CommunityPostImageRepository,
    private val cacheService: CommunityPostCacheService,
    private val userService: CommunityPostUserService
) {
    
    private val logger = KotlinLogging.logger {}

    fun getActivePostById(postId: UUID): CommunityPost {
        // 캐시 우선 조회
        val cachedPost = cacheService.getCachedPostWithAuthor(postId)
        if (cachedPost != null) {
            logger.debug { "Cache hit for post: $postId" }
            return cachedPost.toCommunityPost()
        }
        
        // 캐시 미스 시 DB에서 조회
        val post = communityPostRepository.findActivePostById(postId)
            .orElseThrow { IllegalArgumentException("Post not found or deleted: $postId") }
            
        // 조회된 게시물은 개별적으로 캐시 (사용자 정보 없이)
        // cacheService.cachePost(post) // CommunityPostWithAuthor가 필요하므로 여기서는 캐시하지 않음
        logger.debug { "Retrieved post from DB: $postId" }
        
        return post
    }
    
    /**
     * 사용자 정보와 함께 단일 게시물 조회
     */
    fun getActivePostWithAuthor(postId: UUID): CommunityPostWithAuthor {
        val post = getActivePostById(postId)
        val author = userService.getUserProfile(post.userId)
            ?: userService.createFallbackAuthorInfo(post.userId)
        
        val postWithAuthor = CommunityPostWithAuthor(
            post = post,
            author = author
        )
        
        // 사용자 정보와 함께 캐시
        cacheService.cachePost(postWithAuthor)
        
        return postWithAuthor
    }

    /**
     * 최신 게시물 목록 조회 (캐시 + 로컬 프로젝션)
     */
    fun getActivePosts(pageable: Pageable): Page<CommunityPost> {
        val pageNumber = pageable.pageNumber + 1 // 0-based to 1-based
        
        // 캐시에서 게시물 ID 목록 조회
        val cachedPostIds = cacheService.getLatestPostList(pageNumber)

        return if (cachedPostIds != null) {
            getFromCache(cachedPostIds, pageable, pageNumber)
        } else {
            getFromDbAndWarmCache(pageable, pageNumber)
        }
    }

    private fun getFromCache(
        cachedIds: List<UUID>,
        pageable: Pageable,
        pageNumber: Int
    ): Page<CommunityPost> {
        logger.debug { "Cache hit for post list page: $pageNumber" }

        // 캐시에서 게시물 내용 조회 (MGET)
        val cachedPosts = cacheService.getPosts(cachedIds)

        // 캐시 미스 게시물들 DB에서 조회
        val missingIds = cachedIds.filterNot { cachedPosts.containsKey(it) }
        val missingPosts = if (missingIds.isNotEmpty()) {
            val dbPosts = communityPostRepository.findActivePostsByIds(missingIds)
            // 누락된 게시물들 캐시에 저장
            cacheService.cachePosts(dbPosts)
            logger.debug { "Cached ${dbPosts.size} missing posts from DB" }
            dbPosts.associateBy { it.id }
        } else emptyMap()

        // 결과 조합
        val allPosts = cachedPosts + missingPosts
        val orderedPosts = cachedIds.mapNotNull { allPosts[it] }

        // 전체 엘리먼트 수 조회
        val totalElements = communityPostRepository.countActivePosts()

        return PageImpl(orderedPosts, pageable, totalElements)
    }

    private fun getFromDbAndWarmCache(
        pageable: Pageable,
        pageNumber: Int
    ): Page<CommunityPost> {
        logger.debug { "Cache miss for post list page: $pageNumber, querying DB" }

        // 캐시 미스 시 DB 조회
        val dbResult = communityPostRepository.findActivePosts(pageable)

        // 게시물 ID 목록 캐시에 저장
        val postIds = dbResult.content.map { it.id }
        cacheService.cacheLatestPosts(pageNumber, postIds)

        // 게시물 내용들도 캐시에 저장
        cacheService.cachePosts(dbResult.content)

        logger.debug { "Cached post list page: $pageNumber and ${dbResult.content.size} posts" }

        return dbResult
    }
    
    /**
     * 사용자 정보와 함께 최신 게시물 목록 조회 (로컬 프로젝션)
     * 외부 서비스 호출 없이 안정적인 응답 보장
     */
    fun getActivePostsWithAuthors(userId: UUID, pageable: Pageable): Page<CommunityPostWithAuthor> {
        // 1. 게시물 목록 조회 (캐시 우선)
        val postsPage = getActivePosts(pageable)
        
        if (postsPage.content.isEmpty()) {
            return PageImpl(emptyList(), pageable, 0)
        }
        
        // 2. 사용자 ID 추출
        val userIds = postsPage.content.map { it.userId }.distinct()
        
        // 3. 로컬 프로젝션에서 사용자 정보 배치 조회
        val userProfiles = userService.getUserProfiles(userIds)
        
        logger.debug { "Retrieved ${userProfiles.size} user profiles from local projection for ${postsPage.content.size} posts" }
        
        // 4. 게시물 이미지 정보 배치 조회 (캐시에서 온 데이터는 lazy loading이 안될 수 있음)
        val postIds = postsPage.content.map { it.id }
        val postImages = if (postIds.isNotEmpty()) {
            // 더 효율적인 쿼리 - IN 절 사용
            postIds.flatMap { postId ->
                communityPostImageRepository.findByPostIdOrderByCreatedAtAsc(postId)
                    .map { postId to it }
            }.groupBy({ it.first }, { it.second })
        } else emptyMap()
        
        // 5. 결과 조합 (이미지 포함)
        val postsWithAuthors = postsPage.content.map { post ->
            val author = userProfiles[post.userId] 
                ?: userService.createFallbackAuthorInfo(post.userId)
            
            // 게시물 이미지 정보 - JPA 엔티티의 경우 images 필드에 직접 접근
            // 만약 lazy loading이 제대로 작동하지 않는다면 명시적으로 조회한 이미지 사용
            val images = try {
                post.images // lazy loading 시도
            } catch (e: Exception) {
                postImages[post.id] ?: emptyList() // fallback to explicit query
            }
            
            CommunityPostWithAuthor(
                post = post,
                author = author
            )
        }
        
        return PageImpl(postsWithAuthors, pageable, postsPage.totalElements)
    }

    fun getPopularPosts(pageable: Pageable): Page<CommunityPost> {
        // 인기 게시물은 모든 사용자가 공통으로 조회하므로 캐시 효과가 높음
        // TODO: popular:posts:p{page} 키로 목록 자체도 캐시 적용 고려
        val result = communityPostRepository.findPopularPosts(pageable)
        
        // 개별 게시물을 전역 캐시에 저장
        cacheService.cachePosts(result.content)
        
        return result
    }
    
    /**
     * 사용자 정보와 함께 인기 게시물 목록 조회
     */
    fun getPopularPostsWithAuthors(pageable: Pageable): Page<CommunityPostWithAuthor> {
        val postsPage = getPopularPosts(pageable)
        
        if (postsPage.content.isEmpty()) {
            return PageImpl(emptyList(), pageable, 0)
        }
        
        val userIds = postsPage.content.map { it.userId }.distinct()
        val userProfiles = userService.getUserProfiles(userIds)
        
        val postsWithAuthors = postsPage.content.map { post ->
            val author = userProfiles[post.userId] 
                ?: userService.createFallbackAuthorInfo(post.userId)
            
            CommunityPostWithAuthor(
                post = post,
                author = author
            )
        }
        
        return PageImpl(postsWithAuthors, pageable, postsPage.totalElements)
    }
    
    /**
     * 사용자 정보와 함께 특정 사용자의 게시물 목록 조회
     */
    fun getUserPostsWithAuthor(userId: UUID, pageable: Pageable): Page<CommunityPostWithAuthor> {
        val postsPage = communityPostRepository.findActivePostsByUserId(userId, pageable)
        
        if (postsPage.content.isEmpty()) {
            return PageImpl(emptyList(), pageable, 0)
        }
        
        // 동일 사용자이므로 한 번만 조회
        val userProfile = userService.getUserProfile(userId)
            ?: userService.createFallbackAuthorInfo(userId)
        
        val postsWithAuthors = postsPage.content.map { post ->
            CommunityPostWithAuthor(
                post = post,
                author = userProfile
            )
        }
        
        return PageImpl(postsWithAuthors, pageable, postsPage.totalElements)
    }

    fun getUserPostCount(userId: UUID): Long {
        return communityPostRepository.countActivePostsByUserId(userId)
    }
}