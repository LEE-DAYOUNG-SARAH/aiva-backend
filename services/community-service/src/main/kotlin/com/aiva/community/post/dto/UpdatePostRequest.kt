package com.aiva.community.post.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.*

data class UpdatePostRequest(
    @field:NotBlank(message = "Title cannot be blank")
    @field:Size(max = 50, message = "Title cannot exceed 50 characters")
    val title: String,

    @field:NotBlank(message = "Content cannot be blank")
    @field:Size(max = 1000, message = "Content cannot exceed 1000 characters")
    val content: String,

    val images: List<UpdatePostImageRequest>
)

data class UpdatePostImageRequest(
    val imageId: UUID?,
    val s3Url: String,
    val isDeleted: Boolean
)
