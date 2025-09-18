package com.aiva.community.domain.post.dto

data class LikeResponse(
    val isLiked: Boolean,
    val likeCount: Long
)