package com.aiva.common.redis.auth

import java.util.*

/**
 * Auth 관련 Redis 키 생성기
 */
object AuthRedisKeys {
    
    // 사용자 정보 캐시
    fun userEmail(userId: UUID): String = "user:$userId:email"
    fun userNickname(userId: UUID): String = "user:$userId:nickname"
    
    // JWT 토큰 관리
    fun refreshToken(userId: UUID): String = "refresh_token:$userId"
    fun blacklistedToken(token: String): String = "blacklist_token:${token.hashCode()}"
}