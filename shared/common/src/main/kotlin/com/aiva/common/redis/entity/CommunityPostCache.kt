package com.aiva.common.redis.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import java.time.LocalDateTime
import java.util.*

/**
 * Redis Hash에 저장되는 커뮤니티 게시물 캐시 엔티티
 * SortedSet과 함께 사용하여 최신글 목록 관리
 */
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
    
    @TimeToLive
    val ttl: Long = 24 * 60 * 60L // 24시간
)