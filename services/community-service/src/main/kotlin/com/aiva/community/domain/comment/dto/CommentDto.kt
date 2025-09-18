package com.aiva.community.domain.comment.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime
import java.util.*

data class CreateCommentRequest(
    @field:NotBlank(message = "Content cannot be blank")
    @field:Size(max = 300, message = "Content cannot exceed 300 characters")
    val content: String
)

data class UpdateCommentRequest(
    @field:NotBlank(message = "Content cannot be blank")
    @field:Size(max = 300, message = "Content cannot exceed 300 characters")
    val content: String
)

data class CommentResponse(
    val id: UUID,
    val postId: UUID,
    val userId: UUID,
    val userName: String? = null,
    val userProfileImage: String? = null,
    val parentCommentId: UUID? = null,
    val content: String,
    val likeCount: Int,
    val isLiked: Boolean = false,
    val isReported: Boolean = false,
    val replies: List<CommentResponse> = emptyList(),
    val replyCount: Int = 0,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class CommentSummaryResponse(
    val id: UUID,
    val content: String,
    val likeCount: Int,
    val replyCount: Int,
    val createdAt: LocalDateTime
)