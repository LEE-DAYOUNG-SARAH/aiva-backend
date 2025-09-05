package com.aiva.community.post.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreatePostRequest(
    @field:NotBlank(message = "Title cannot be blank")
    @field:Size(max = 50, message = "Title cannot exceed 50 characters")
    val title: String,

    @field:NotBlank(message = "Content cannot be blank")
    @field:Size(max = 1000, message = "Content cannot exceed 1000 characters")
    val content: String,

    @field:Size(max = 3, message = "Cannot have more than 3 images")
    val imageUrls: List<String> = emptyList()
)