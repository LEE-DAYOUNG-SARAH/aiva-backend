package com.aiva.common.redis.service

import com.aiva.common.redis.entity.CommunityPostCache
import com.aiva.common.redis.repository.CommunityPostCacheRepository
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class RedisCommunityServiceV2(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val objectMapper: ObjectMapper,
    private val communityPostCacheRepository: CommunityPostCacheRepository
) {
    
    private val logger = KotlinLogging.logger {}
    
    companion object {
        // Hash 구조로 개별 게시물 저장 (좋아요/댓글 수 업데이트용)
        private const val POST_HASH_PREFIX = "community:post:"
        
        // SortedSet으로 최신글 목록 관리 (timestamp를 score로 사용)
        private const val LATEST_POSTS_SORTED_SET = "community:posts:latest"
        
        // 사용자 프로필 캐시 (Hash 구조)
        private const val USER_PROFILE_PREFIX = "user:"
        private const val USER_PROFILE_SUFFIX = ":profile"
        
        // TTL 24시간 (초 단위)
        private const val USER_PROFILE_TTL_SECONDS = 24 * 60 * 60L
    }
    
    /**
     * 게시물을 Hash 구조로 저장하고 SortedSet에 추가
     */
    fun savePost(postCache: CommunityPostCache) {
        try {
            val timestamp = postCache.createdAt.toEpochSecond(ZoneOffset.UTC).toDouble()
            
            // 1. Repository를 사용하여 Hash로 게시물 저장 (TTL 자동 적용)
            communityPostCacheRepository.save(postCache)
            
            // 2. SortedSet에 추가 (최신순 정렬을 위해 timestamp를 score로 사용)
            redisTemplate.opsForZSet().add(LATEST_POSTS_SORTED_SET, postCache.id.toString(), timestamp)
            
            logger.debug { "Saved post ${postCache.id} to Redis with Repository + SortedSet structure" }
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to save post ${postCache.id} to Redis" }
        }
    }
    
    /**
     * Hash에서 게시물 조회
     */
    fun getPost(postId: UUID): CommunityPostCache? {
        return try {
            // Repository를 사용하여 Hash에서 조회
            communityPostCacheRepository.findById(postId).orElse(null)
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to get post $postId from Redis" }
            null
        }
    }
    
    /**
     * SortedSet을 이용한 최신글 목록 조회 (오프셋 기반 - 호환성용)
     */
    fun getLatestPosts(page: Int, pageSize: Int = 20): List<UUID> {
        return try {
            val start = (page - 1) * pageSize
            val end = start + pageSize - 1
            
            // SortedSet에서 최신순(역순)으로 조회
            val postIds = redisTemplate.opsForZSet()
                .reverseRange(LATEST_POSTS_SORTED_SET, start.toLong(), end.toLong())
                ?: emptySet()
            
            val result = postIds.mapNotNull { 
                try {
                    UUID.fromString(it.toString())
                } catch (e: Exception) {
                    logger.warn { "Invalid UUID in SortedSet: $it" }
                    null
                }
            }
            
            logger.debug { "Retrieved ${result.size} latest posts from SortedSet (page: $page)" }
            result
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to get latest posts from SortedSet" }
            emptyList()
        }
    }
    
    /**
     * 커서 기반 최신글 목록 조회
     */
    fun getLatestPostsByCursor(lastScore: Double?, limit: Int = 20): List<UUID> {
        return try {
            // 커서가 있는 경우 해당 점수보다 작은 것들만 조회 (open range)
            val postIds = if (lastScore != null) {
                redisTemplate.opsForZSet()
                    .reverseRangeByScore(LATEST_POSTS_SORTED_SET, Double.NEGATIVE_INFINITY, lastScore, 0, limit.toLong())
                    ?.filter { 
                        // 같은 score를 가진 경우를 위해 추가 필터링은 애플리케이션에서 처리
                        true  
                    }
            } else {
                // 첫 페이지: 가장 최신부터
                redisTemplate.opsForZSet()
                    .reverseRangeByScore(LATEST_POSTS_SORTED_SET, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0, limit.toLong())
            } ?: emptySet()
            
            val result = postIds.mapNotNull { 
                try {
                    UUID.fromString(it.toString())
                } catch (e: Exception) {
                    logger.warn { "Invalid UUID in SortedSet: $it" }
                    null
                }
            }
            
            logger.debug { "Retrieved ${result.size} latest posts from SortedSet with cursor (lastScore: $lastScore)" }
            result
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to get latest posts from SortedSet with cursor" }
            emptyList()
        }
    }
    
    /**
     * SortedSet과 Hash를 조합한 최신글 목록 조회 (상세 정보 포함)
     */
    fun getLatestPostsWithDetails(page: Int, pageSize: Int = 20): List<CommunityPostCache> {
        val postIds = getLatestPosts(page, pageSize)
        
        return postIds.mapNotNull { postId ->
            getPost(postId)
        }
    }
    
    /**
     * 게시물 삭제 (Hash에서 삭제하고 SortedSet에서도 제거)
     */
    fun deletePost(postId: UUID) {
        try {
            val postKey = "$POST_HASH_PREFIX$postId"
            
            // Hash에서 삭제
            redisTemplate.delete(postKey)
            
            // SortedSet에서 제거
            redisTemplate.opsForZSet().remove(LATEST_POSTS_SORTED_SET, postId.toString())
            
            logger.debug { "Deleted post $postId from Redis" }
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete post $postId from Redis" }
        }
    }
    
    /**
     * 좋아요 수 증가
     */
    fun incrementLikeCount(postId: UUID): Boolean {
        return try {
            val postKey = "$POST_HASH_PREFIX$postId"
            
            // Hash에서 좋아요 수 증가
            val newCount = redisTemplate.opsForHash<String, Any>().increment(postKey, "likeCount", 1)
            
            logger.debug { "Incremented like count for post $postId to $newCount" }
            true
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to increment like count for post $postId" }
            false
        }
    }
    
    /**
     * 좋아요 수 감소
     */
    fun decrementLikeCount(postId: UUID): Boolean {
        return try {
            val postKey = "$POST_HASH_PREFIX$postId"
            
            // Hash에서 좋아요 수 감소 (최소 0)
            val currentCount = redisTemplate.opsForHash<String, Any>().get(postKey, "likeCount")?.toString()?.toIntOrNull() ?: 0
            
            if (currentCount > 0) {
                val newCount = redisTemplate.opsForHash<String, Any>().increment(postKey, "likeCount", -1)
                logger.debug { "Decremented like count for post $postId to $newCount" }
                return true
            }
            
            false
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to decrement like count for post $postId" }
            false
        }
    }
    
    /**
     * SortedSet의 총 개수 조회
     */
    fun getTotalPostCount(): Long {
        return try {
            redisTemplate.opsForZSet().size(LATEST_POSTS_SORTED_SET) ?: 0L
        } catch (e: Exception) {
            logger.error(e) { "Failed to get total post count" }
            0L
        }
    }
    
    /**
     * 오래된 게시물 정리 (배치 작업용)
     * SortedSet에서 특정 시간보다 오래된 게시물들을 제거
     */
    fun cleanupOldPosts(olderThanHours: Long = 48) {
        try {
            val cutoffTime = LocalDateTime.now().minusHours(olderThanHours)
            val cutoffTimestamp = cutoffTime.toEpochSecond(ZoneOffset.UTC).toDouble()
            
            // SortedSet에서 오래된 게시물 ID들 조회
            val oldPostIds = redisTemplate.opsForZSet()
                .rangeByScore(LATEST_POSTS_SORTED_SET, 0.0, cutoffTimestamp)
                ?: emptySet()
            
            // 각 게시물에 대해 Hash와 SortedSet에서 삭제
            oldPostIds.forEach { postIdStr ->
                try {
                    val postId = UUID.fromString(postIdStr.toString())
                    deletePost(postId)
                } catch (e: Exception) {
                    logger.warn { "Failed to cleanup old post: $postIdStr" }
                }
            }
            
            logger.info { "Cleaned up ${oldPostIds.size} old posts older than $olderThanHours hours" }
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to cleanup old posts" }
        }
    }
    
    /**
     * 사용자 프로필 정보 캐싱
     */
    fun cacheUserProfile(userId: UUID, nickname: String, profileUrl: String?) {
        try {
            val userKey = "$USER_PROFILE_PREFIX$userId$USER_PROFILE_SUFFIX"
            val profileMap = mutableMapOf<String, Any>(
                "userId" to userId.toString(),
                "nickname" to nickname
            )
            
            // profileUrl이 있는 경우에만 추가
            profileUrl?.let { profileMap["profileUrl"] = it }
            
            redisTemplate.opsForHash<String, Any>().putAll(userKey, profileMap)
            redisTemplate.expire(userKey, USER_PROFILE_TTL_SECONDS, TimeUnit.SECONDS)
            
            logger.debug { "Cached user profile for userId: $userId" }
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to cache user profile for userId: $userId" }
        }
    }
    
    /**
     * 사용자 프로필 정보 조회
     */
    fun getUserProfile(userId: UUID): Map<String, Any>? {
        return try {
            val userKey = "$USER_PROFILE_PREFIX$userId$USER_PROFILE_SUFFIX"
            val profileMap = redisTemplate.opsForHash<String, Any>().entries(userKey)
            
            if (profileMap.isEmpty()) {
                logger.debug { "Cache miss for user profile: $userId" }
                return null
            }
            
            logger.debug { "Cache hit for user profile: $userId" }
            profileMap
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to get user profile for userId: $userId" }
            null
        }
    }
    
    /**
     * 여러 사용자 프로필 정보 배치 조회
     */
    fun getUserProfiles(userIds: Collection<UUID>): Map<UUID, Map<String, Any>> {
        if (userIds.isEmpty()) return emptyMap()
        
        return try {
            val result = mutableMapOf<UUID, Map<String, Any>>()
            
            userIds.forEach { userId ->
                getUserProfile(userId)?.let { profile ->
                    result[userId] = profile
                }
            }
            
            logger.debug { "Retrieved ${result.size} user profiles from cache out of ${userIds.size} requested" }
            result
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to get user profiles for userIds: $userIds" }
            emptyMap()
        }
    }
    
    /**
     * 사용자 프로필 캐시 삭제 (사용자 정보 업데이트 시 사용)
     */
    fun evictUserProfile(userId: UUID) {
        try {
            val userKey = "$USER_PROFILE_PREFIX$userId$USER_PROFILE_SUFFIX"
            redisTemplate.delete(userKey)
            
            logger.debug { "Evicted user profile cache for userId: $userId" }
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to evict user profile cache for userId: $userId" }
        }
    }
}