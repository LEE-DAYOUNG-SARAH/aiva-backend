package com.aiva.user.dto

import jakarta.validation.constraints.NotBlank

data class UserAvatarUpdateRequest(
    @field:NotBlank(message = "아바타 URL은 필수입니다")
    val avatarUrl: String
)

data class UserAvatarUpdateResponse(
    val avatarUrl: String
)
