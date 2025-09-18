package com.aiva.community.domain.post.dto

import com.aiva.community.domain.user.AuthorInfo
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