package com.aiva.security.dto

import java.util.*

/**
 * 인증된 사용자 정보
 * JWT 토큰에서 추출한 사용자 정보를 담는 클래스
 */
data class UserPrincipal(
    val userId: UUID,
    val email: String?,
    val nickname: String
) {
    companion object {
        const val USER_ID_HEADER = "X-User-Id"
        const val USER_EMAIL_HEADER = "X-User-Email"
        const val USER_NICKNAME_HEADER = "X-User-Nickname"
    }
}