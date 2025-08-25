package com.aiva.user.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserAvatarUpdateRequest(
    @field:NotBlank(message = "아바타 URL은 필수입니다")
    val avatarUrl: String
)

data class UserAvatarUpdateResponse(
    val avatarUrl: String
)

data class UserInfoUpdateRequest(
    @field:NotBlank(message = "닉네임은 필수입니다")
    @field:Size(min = 1, max = 10, message = "닉네임은 1-10자여야 합니다")
    val nickname: String,

    val childInfo: ChildRequest
)

data class UserInfoUpdateResponse(
    val nickname: String,
    val childInfo: ChildResponse
)