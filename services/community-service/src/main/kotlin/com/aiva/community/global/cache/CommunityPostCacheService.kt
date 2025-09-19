package com.aiva.community.global.cache

import com.aiva.community.domain.post.entity.CommunityPost
import com.aiva.community.domain.post.dto.CommunityPostWithAuthor
import com.aiva.common.redis.entity.CommunityPostCache
import com.aiva.common.redis.repository.CommunityPostCacheRepository
import com.aiva.common.redis.service.RedisCommunityService
import com.aiva.community.global.cache.toCommunityPost
import com.aiva.community.global.cache.toCommunityPostCache
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*

@Service
class CommunityPostCacheService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val objectMapper: ObjectMapper,
    private val redisCommunityService: RedisCommunityService,
    private val communityPostCacheRepository: CommunityPostCacheRepository
) {
    
    private val logger = LoggerFactory.getLogger(CommunityPostCacheService::class.java)
    
    companion object {
        private const val POST_KEY_PREFIX = "post:"
        
        // 최신순 목록 캐시 키
        private const val LATEST_LIST_PREFIX = "list:posts:latest:p"
        private const val LATEST_LIST_VERSION_KEY = "list:posts:latest:ver"
        
        // 인기순 목록 캐시 키
        private const val POPULAR_LIST_PREFIX = "list:posts:popular:p"
        private const val POPULAR_LIST_VERSION_KEY = "list:posts:popular:ver"
        
        private const val USER_KEY_PREFIX = "user:"
        
        private val POST_TTL = Duration.ofMinutes(30)
        private val LATEST_LIST_TTL = Duration.ofSeconds(60)  // 최신순은 짧게
        private val POPULAR_LIST_TTL = Duration.ofMinutes(30) // 인기순은 길게
        private val USER_TTL = Duration.ofHours(1)
    }
    
    // ============ Post 캐시 Operations ============

    /**
     * 게시물과 작성자 정보를 함께 Redis에 저장
     * Spring Data Redis Repository를 사용하여 구조화된 데이터로 저장
     */
    fun cachePost(postWithAuthor: CommunityPostWithAuthor) {
        try {
            val postCache = postWithAuthor.toCommunityPostCache()
            communityPostCacheRepository.save(postCache)
            logger.debug("Cached post with author: {}", postWithAuthor.post.id)
        } catch (e: Exception) {
            logger.error("Failed to cache post with author: ${postWithAuthor.post.id}", e)
        }
    }

    
    /**
     * Redis에서 게시물과 작성자 정보를 함께 조회
     */
    fun getCachedPostWithAuthor(postId: UUID): CommunityPostCache? {
        return try {
            communityPostCacheRepository.findById(postId).orElse(null)
        } catch (e: Exception) {
            logger.error("Failed to get cached post with author: $postId", e)
            null
        }
    }
    
    fun evictPost(postId: UUID) {
        try {
            // 기존 방식 삭제
            val key = "$POST_KEY_PREFIX$postId"
            redisTemplate.delete(key)
            
            // 새로운 방식 삭제
            communityPostCacheRepository.deleteById(postId)
            
            logger.debug("Evicted post: {}", postId)
        } catch (e: Exception) {
            logger.error("Failed to evict post: $postId", e)
        }
    }
    
    // ============ Post List 캐시 Operations ============
    
    // === 최신순 목록 캐시 ===
    
    fun cacheLatestPosts(page: Int, postIds: List<UUID>) {
        try {
            val key = "$LATEST_LIST_PREFIX$page"
            val postIdsJson = objectMapper.writeValueAsString(postIds)
            redisTemplate.opsForValue().set(key, postIdsJson, LATEST_LIST_TTL)
            logger.debug("Cached latest post list page: {}, size: {}", page, postIds.size)
        } catch (e: Exception) {
            logger.error("Failed to cache latest post list page: $page", e)
        }
    }
    
    fun getLatestPostList(page: Int): List<UUID>? {
        return try {
            val key = "$LATEST_LIST_PREFIX$page"
            val postIdsJson = redisTemplate.opsForValue().get(key)
            postIdsJson?.let {
                objectMapper.readValue(it.toString(), objectMapper.typeFactory.constructCollectionType(List::class.java, UUID::class.java))
            }
        } catch (e: Exception) {
            logger.error("Failed to get cached latest post list page: $page", e)
            null
        }
    }
    
    fun evictLatestPostPages(startPage: Int = 1, endPage: Int = 5) {
        try {
            val keys = (startPage..endPage).map { "$LATEST_LIST_PREFIX$it" }
            redisTemplate.delete(keys)
            logger.debug("Evicted latest post list pages: {} to {}", startPage, endPage)
        } catch (e: Exception) {
            logger.error("Failed to evict latest post list pages", e)
        }
    }
    
    // === 인기순 목록 캐시 ===
    
    fun cachePopularPosts(page: Int, postIds: List<UUID>) {
        try {
            val key = "$POPULAR_LIST_PREFIX$page"
            val postIdsJson = objectMapper.writeValueAsString(postIds)
            redisTemplate.opsForValue().set(key, postIdsJson, POPULAR_LIST_TTL)
            logger.debug("Cached popular post list page: {}, size: {}", page, postIds.size)
        } catch (e: Exception) {
            logger.error("Failed to cache popular post list page: $page", e)
        }
    }
    
    fun getPopularPostList(page: Int): List<UUID>? {
        return try {
            val key = "$POPULAR_LIST_PREFIX$page"
            val postIdsJson = redisTemplate.opsForValue().get(key)
            postIdsJson?.let {
                objectMapper.readValue(it.toString(), objectMapper.typeFactory.constructCollectionType(List::class.java, UUID::class.java))
            }
        } catch (e: Exception) {
            logger.error("Failed to get cached popular post list page: $page", e)
            null
        }
    }
    
    fun evictPopularPostPages(startPage: Int = 1, endPage: Int = 5) {
        try {
            val keys = (startPage..endPage).map { "$POPULAR_LIST_PREFIX$it" }
            redisTemplate.delete(keys)
            logger.debug("Evicted popular post list pages: {} to {}", startPage, endPage)
        } catch (e: Exception) {
            logger.error("Failed to evict popular post list pages", e)
        }
    }
    
    // ============ Version Management ============
    
    // === 최신순 버전 관리 ===
    
    fun incrementLatestListVersion(): Long {
        return try {
            val newVersion = redisTemplate.opsForValue().increment(LATEST_LIST_VERSION_KEY) ?: 1L
            logger.debug("Incremented latest list version to: {}", newVersion)
            newVersion
        } catch (e: Exception) {
            logger.error("Failed to increment latest list version", e)
            1L
        }
    }
    
    fun getLatestListVersion(): Long {
        return try {
            val version = redisTemplate.opsForValue().get(LATEST_LIST_VERSION_KEY)?.toString()?.toLongOrNull() ?: 0L
            logger.debug("Current latest list version: {}", version)
            version
        } catch (e: Exception) {
            logger.error("Failed to get latest list version", e)
            0L
        }
    }
    
    // === 인기순 버전 관리 ===
    
    fun incrementPopularListVersion(): Long {
        return try {
            val newVersion = redisTemplate.opsForValue().increment(POPULAR_LIST_VERSION_KEY) ?: 1L
            logger.debug("Incremented popular list version to: {}", newVersion)
            newVersion
        } catch (e: Exception) {
            logger.error("Failed to increment popular list version", e)
            1L
        }
    }
    
    fun getPopularListVersion(): Long {
        return try {
            val version = redisTemplate.opsForValue().get(POPULAR_LIST_VERSION_KEY)?.toString()?.toLongOrNull() ?: 0L
            logger.debug("Current popular list version: {}", version)
            version
        } catch (e: Exception) {
            logger.error("Failed to get popular list version", e)
            0L
        }
    }
    
    // ============ User Profile 캐시 (로컬 프로젝션 + 캐시 전략) ============
    
    // ============ Bulk Operations for Performance ============
    
    /**
     * 여러 게시물을 한 번에 캐시에 저장 (CommunityPost -> CommunityPostCache 변환 후 저장)
     */
    fun cachePosts(posts: List<CommunityPost>) {
        try {
            val postCaches = posts.map { post ->
                // 기본 사용자 정보로 CommunityPostCache 생성
                post.toCommunityPostCache("Unknown", null)
            }
            
            if (postCaches.isNotEmpty()) {
                communityPostCacheRepository.saveAll(postCaches)
                logger.debug("Bulk cached {} posts using repository", posts.size)
            }
        } catch (e: Exception) {
            logger.error("Failed to bulk cache posts using repository", e)
        }
    }
    
    /**
     * 여러 게시물 ID로 캐시된 게시물들을 조회 (CommunityPostCache -> CommunityPost 변환)
     */
    fun getPosts(postIds: List<UUID>): Map<UUID, CommunityPost> {
        val result = mutableMapOf<UUID, CommunityPost>()
        
        try {
            val cachedPosts = communityPostCacheRepository.findAllById(postIds)
            
            cachedPosts.forEach { cachedPost ->
                result[cachedPost.id] = cachedPost.toCommunityPost()
            }
            
            logger.debug("Retrieved {} cached posts out of {} using repository", result.size, postIds.size)
        } catch (e: Exception) {
            logger.error("Failed to get cached posts using repository", e)
        }
        
        return result
    }
    
    /**
     * Repository를 활용한 최신순 게시물 조회
     */
    fun getLatestPostsFromCache(limit: Int = 20): List<CommunityPost> {
        return try {
            communityPostCacheRepository.findAllByOrderByCreatedAtDesc()
                .take(limit)
                .map { it.toCommunityPost() }
        } catch (e: Exception) {
            logger.error("Failed to get latest posts from cache", e)
            emptyList()
        }
    }
    
    /**
     * Repository를 활용한 인기순 게시물 조회
     */
    fun getPopularPostsFromCache(limit: Int = 20): List<CommunityPost> {
        return try {
            communityPostCacheRepository.findAllByOrderByLikeCountDescCreatedAtDesc()
                .take(limit)
                .map { it.toCommunityPost() }
        } catch (e: Exception) {
            logger.error("Failed to get popular posts from cache", e)
            emptyList()
        }
    }
    
    /**
     * Repository를 활용한 특정 사용자의 게시물 조회
     */
    fun getUserPostsFromCache(authorId: UUID, limit: Int = 20): List<CommunityPost> {
        return try {
            communityPostCacheRepository.findByAuthorId(authorId)
                .take(limit)
                .map { it.toCommunityPost() }
        } catch (e: Exception) {
            logger.error("Failed to get user posts from cache for user: $authorId", e)
            emptyList()
        }
    }
    
    /**
     * Repository를 활용한 전체 캐시 데이터 개수 조회
     */
    fun getCachedPostsCount(): Long {
        return try {
            communityPostCacheRepository.count()
        } catch (e: Exception) {
            logger.error("Failed to get cached posts count", e)
            0L
        }
    }
    
    // ============ 배치 전용 Operations ============
    
    /**
     * 배치에서 인기순 목록을 전체적으로 업데이트
     * 주기적으로 실행되어 좋아요/댓글 수 기반의 인기순 데이터를 새로 구성
     */
    fun cachePopularPostsBatch(postsWithAuthors: List<CommunityPostWithAuthor>, totalPages: Int) {
        try {
            logger.info("Starting batch cache update for popular posts. Total posts: {}, Total pages: {}", 
                postsWithAuthors.size, totalPages)
            
            // 1. 기존 인기순 캐시 전체 삭제
            evictPopularPostPages(startPage = 1, endPage = totalPages)
            
            // 2. 개별 게시물들을 캐시에 일괄 저장
            val postCaches = postsWithAuthors.map { it.toCommunityPostCache() }
            communityPostCacheRepository.saveAll(postCaches)
            
            // 3. 페이지별로 목록 업데이트
            val pageSize = 20 // 기본 페이지 크기
            val groupedPosts = postsWithAuthors.chunked(pageSize)
            
            groupedPosts.forEachIndexed { index, pageData ->
                val page = index + 1
                val postIds = pageData.map { it.post.id }
                cachePopularPosts(page, postIds)
            }
            
            // 4. 버전 증가로 무효화 감지
            incrementPopularListVersion()
            
            logger.info("Completed batch cache update for popular posts. Updated {} pages", 
                groupedPosts.size)
            
        } catch (e: Exception) {
            logger.error("Failed to batch cache popular posts", e)
        }
    }
    
    /**
     * 인기순 점수 계산 (간단한 알고리즘)
     * 실제로는 더 복잡한 알고리즘 사용 가능 (시간 가중치, 사용자 상호작용 등)
     */
    private fun calculatePopularityScore(likeCount: Int, commentCount: Int): Double {
        // 간단한 가중치: 좋아요 1점, 댓글 2점
        return likeCount.toDouble() + (commentCount * 2.0)
    }
    
    /**
     * 배치 실행 상태 확인 (모니터링용)
     */
    fun getPopularCacheBatchStatus(): Map<String, Any> {
        return mapOf(
            "popularListVersion" to getPopularListVersion(),
            "latestListVersion" to getLatestListVersion(),
            "cacheTimestamp" to System.currentTimeMillis(),
            "popularCachePages" to (1..5).map { page ->
                mapOf(
                    "page" to page,
                    "cached" to (getPopularPostList(page) != null),
                    "size" to (getPopularPostList(page)?.size ?: 0)
                )
            }
        )
    }
}