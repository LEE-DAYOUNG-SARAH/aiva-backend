package com.aiva.common.redis.service

import com.aiva.common.redis.entity.CommunityPostCache
import com.aiva.common.redis.repository.CommunityPostCacheRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class RedisCommunityService(
    private val communityPostCacheRepository: CommunityPostCacheRepository
) {
    
    fun cachePost(
        postId: String,
        title: String,
        content: String,
        authorId: UUID,
        authorNickname: String,
        imageUrls: List<String> = emptyList(),
        likeCount: Int = 0,
        commentCount: Int = 0,
        createdAt: LocalDateTime,
        updatedAt: LocalDateTime,
        authorProfileImageUrl: String? = null
    ): CommunityPostCache {
        val postCache = CommunityPostCache(
            id = UUID.fromString(postId),
            title = title,
            content = content,
            imageUrls = imageUrls,
            likeCount = likeCount,
            commentCount = commentCount,
            createdAt = createdAt,
            updatedAt = updatedAt,
            authorId = authorId,
            authorNickname = authorNickname,
            authorProfileImageUrl = authorProfileImageUrl
        )
        return communityPostCacheRepository.save(postCache)
    }
    
    fun getCachedPost(postId: String): CommunityPostCache? {
        return communityPostCacheRepository.findById(UUID.fromString(postId)).orElse(null)
    }
    
    fun getCachedPostsByAuthor(authorId: UUID): List<CommunityPostCache> {
        return communityPostCacheRepository.findByAuthorId(authorId)
    }
    
    fun incrementLikeCount(postId: String): CommunityPostCache? {
        return communityPostCacheRepository.findById(UUID.fromString(postId)).orElse(null)?.let { post ->
            val updatedPost = post.incrementLikeCount()
            communityPostCacheRepository.save(updatedPost)
        }
    }
    
    fun decrementLikeCount(postId: String): CommunityPostCache? {
        return communityPostCacheRepository.findById(UUID.fromString(postId)).orElse(null)?.let { post ->
            val updatedPost = post.decrementLikeCount()
            communityPostCacheRepository.save(updatedPost)
        }
    }
    
    fun incrementCommentCount(postId: String): CommunityPostCache? {
        return communityPostCacheRepository.findById(UUID.fromString(postId)).orElse(null)?.let { post ->
            val updatedPost = post.incrementCommentCount()
            communityPostCacheRepository.save(updatedPost)
        }
    }
    
    fun decrementCommentCount(postId: String): CommunityPostCache? {
        return communityPostCacheRepository.findById(UUID.fromString(postId)).orElse(null)?.let { post ->
            val updatedPost = post.decrementCommentCount()
            communityPostCacheRepository.save(updatedPost)
        }
    }
    
    fun deleteCachedPost(postId: String) {
        communityPostCacheRepository.deleteById(UUID.fromString(postId))
    }
    
    fun evictCache(postId: String) {
        communityPostCacheRepository.deleteById(UUID.fromString(postId))
    }
}