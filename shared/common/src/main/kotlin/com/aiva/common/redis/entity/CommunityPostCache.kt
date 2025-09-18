package com.aiva.common.redis.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed
import java.time.LocalDateTime
import java.util.*

@RedisHash("community_post")
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
    
    // 작성자 정보  
    @Indexed
    val authorId: UUID,
    val authorNickname: String,
    val authorProfileImageUrl: String? = null
)