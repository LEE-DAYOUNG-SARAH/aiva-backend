package com.aiva.common.redis.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import org.springframework.data.redis.core.index.Indexed
import java.time.LocalDateTime
import java.util.*

@RedisHash("community:post")
data class CommunityPostCache(
    @Id
    val id: UUID,
    
    val title: String,
    
    val content: String,
    
    val imageUrls: List<String> = emptyList(),
    
    val likeCount: Int = 0,
    
    val commentCount: Int = 0,
    
    val createdAt: LocalDateTime,
    
    val updatedAt: LocalDateTime,
    
    val authorId: UUID,
    
    val authorNickname: String,
    
    val authorProfileImageUrl: String? = null,
    
    @TimeToLive
    val ttl: Long = 60 * 60L // 1 hour in seconds
) {
    fun incrementLikeCount(): CommunityPostCache = copy(likeCount = likeCount + 1)
    fun decrementLikeCount(): CommunityPostCache = copy(likeCount = maxOf(0, likeCount - 1))
    fun incrementCommentCount(): CommunityPostCache = copy(commentCount = commentCount + 1)
    fun decrementCommentCount(): CommunityPostCache = copy(commentCount = maxOf(0, commentCount - 1))
}