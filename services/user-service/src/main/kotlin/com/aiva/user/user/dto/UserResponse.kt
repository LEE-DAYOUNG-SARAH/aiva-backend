package com.aiva.user.user.dto

import com.aiva.user.user.entity.User

data class UserResponse(
    val userId: String,
    val email: String?,
    val nickname: String,
    val avatarUrl: String?
) {
    companion object {
        fun from(user: User) = UserResponse(
            userId = user.id.toString(),
            email = user.email,
            nickname = user.nickname,
            avatarUrl = user.avatarUrl
        )
    }
}
