package com.aiva.common.redis.service

import com.aiva.common.redis.entity.CommunityPostCache
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
    private val objectMapper: ObjectMapper
) {
    
    private val logger = KotlinLogging.logger {}
    
    companion object {
        // Hash 구조로 개별 게시물 저장
        private const val POST_HASH_PREFIX = "community:post:"
        
        // SortedSet으로 최신글 목록 관리 (timestamp를 score로 사용)
        private const val LATEST_POSTS_SORTED_SET = "community:posts:latest"
        
        // TTL 24시간 (초 단위)
        private const val POST_TTL_SECONDS = 24 * 60 * 60L
    }
    
    /**
     * 게시물을 Hash 구조로 저장하고 SortedSet에 추가
     */
    fun savePost(postCache: CommunityPostCache) {
        try {
            val postKey = "$POST_HASH_PREFIX${postCache.id}"
            val timestamp = postCache.createdAt.toEpochSecond(ZoneOffset.UTC).toDouble()
            
            // 1. Hash로 게시물 상세 정보 저장
            val postMap = mapOf<String, Any>(
                "id" to postCache.id.toString(),
                "title" to postCache.title,
                "content" to postCache.content,
                "imageUrls" to objectMapper.writeValueAsString(postCache.imageUrls),
                "likeCount" to postCache.likeCount,
                "commentCount" to postCache.commentCount,
                "createdAt" to postCache.createdAt.toString(),
                "updatedAt" to postCache.updatedAt.toString(),
                "authorId" to postCache.authorId.toString()
            )
            
            redisTemplate.opsForHash<String, Any>().putAll(postKey, postMap)
            
            // 2. TTL 24시간 설정
            redisTemplate.expire(postKey, POST_TTL_SECONDS, TimeUnit.SECONDS)
            
            // 3. SortedSet에 추가 (최신순 정렬을 위해 timestamp를 score로 사용)
            redisTemplate.opsForZSet().add(LATEST_POSTS_SORTED_SET, postCache.id.toString(), timestamp)
            
            logger.debug { "Saved post ${postCache.id} to Redis with Hash + SortedSet structure" }
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to save post ${postCache.id} to Redis" }
        }
    }
    
    /**
     * Hash에서 게시물 조회
     */
    fun getPost(postId: UUID): CommunityPostCache? {
        return try {
            val postKey = "$POST_HASH_PREFIX$postId"
            val postMap = redisTemplate.opsForHash<String, Any>().entries(postKey)
            
            if (postMap.isEmpty()) {
                return null
            }
            
            val imageUrls = if (postMap["imageUrls"]?.toString()?.isNotEmpty() == true) {
                objectMapper.readValue(
                    postMap["imageUrls"].toString(),
                    objectMapper.typeFactory.constructCollectionType(List::class.java, String::class.java)
                )
            } else {
                emptyList()
            }
            
            CommunityPostCache(
                id = UUID.fromString(postMap["id"].toString()),
                title = postMap["title"].toString(),
                content = postMap["content"].toString(),
                imageUrls = imageUrls,
                likeCount = postMap["likeCount"].toString().toInt(),
                commentCount = postMap["commentCount"].toString().toInt(),
                createdAt = LocalDateTime.parse(postMap["createdAt"].toString()),
                updatedAt = LocalDateTime.parse(postMap["updatedAt"].toString()),
                authorId = UUID.fromString(postMap["authorId"].toString()),
                ttl = POST_TTL_SECONDS
            )
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to get post $postId from Redis" }
            null
        }
    }
    
    /**
     * SortedSet을 이용한 최신글 목록 조회 (페이징)
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
}