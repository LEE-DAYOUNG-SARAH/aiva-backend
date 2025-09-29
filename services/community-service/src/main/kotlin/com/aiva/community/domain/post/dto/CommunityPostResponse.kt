package com.aiva.community.domain.post.dto

import java.time.LocalDateTime
import java.util.*

data class CommunityPostResponse(
    val id: UUID,
    val title: String,
    val content: String,
    val imageUrls: List<String> = emptyList(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val createdAt: LocalDateTime,
    val author: AuthorInfo
)

data class AuthorInfo(
    val userId: UUID,
    val nickname: String,
    val profileImageUrl: String?
)