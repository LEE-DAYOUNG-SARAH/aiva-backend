package com.aiva.user.auth.dto

import com.aiva.user.user.entity.User

/**
 * 인증 관련 Request/Response DTO들
 */

// 앱에서 OAuth 완료 후 서버로 사용자 정보 전달
data class AppLoginRequest(
    val provider: String,           // "KAKAO" or "GOOGLE"
    val providerUserId: String,     // OAuth 제공자 사용자 ID
    val email: String,             // 이메일
    val nickname: String,           // 닉네임
    val avatarUrl: String?          // 프로필 이미지 URL
)

class AppLoginResponse(
    accessToken: String,
    refreshToken: String,
    tokenType: String = "Bearer",
    expiresIn: Long,            // 초 단위
    user: UserInfo,
    val hasChild: Boolean
): AuthResponse(accessToken, refreshToken, tokenType, expiresIn, user)

open class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,            // 초 단위
    val user: UserInfo
)

data class UserInfo(
    val userId: String,
    val email: String?,
    val nickname: String,
    val avatarUrl: String?
) {
    companion object {
        fun from(user: User) = UserInfo(
            userId = user.id.toString(),
            email = user.email,
            nickname = user.nickname,
            avatarUrl = user.avatarUrl
        )
    }
}

data class RefreshTokenRequest(
    val refreshToken: String
)