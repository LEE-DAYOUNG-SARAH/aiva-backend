package com.aiva.common.redis.auth

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit
import com.aiva.common.redis.auth.AuthRedisKeys

/**
 * 인증 관련 Redis 서비스
 * 현재 AuthService에서 사용하는 메서드들만 구현
 */
@Service
class AuthRedisService(
    private val redisTemplate: RedisTemplate<String, String>
) {
    
    // ================================
    // 1. 사용자 정보 캐싱
    // ================================
    
    /**
     * 사용자 정보 캐시 저장 (7일)
     */
    fun setUserCache(userId: UUID, email: String?, nickname: String) {
        val emailKey = AuthRedisKeys.userEmail(userId)
        val nicknameKey = AuthRedisKeys.userNickname(userId)
        
        redisTemplate.opsForValue().set(emailKey, email ?: "", 7, TimeUnit.DAYS)
        redisTemplate.opsForValue().set(nicknameKey, nickname, 7, TimeUnit.DAYS)
    }
    
    /**
     * 사용자 이메일 조회
     */
    fun getUserEmail(userId: UUID): String? {
        return redisTemplate.opsForValue().get(AuthRedisKeys.userEmail(userId))
    }
    
    /**
     * 사용자 닉네임 조회
     */
    fun getUserNickname(userId: UUID): String? {
        return redisTemplate.opsForValue().get(AuthRedisKeys.userNickname(userId))
    }
    
    // ================================
    // 2. 리프레시 토큰 관리
    // ================================
    
    /**
     * 리프레시 토큰 저장 (30일)
     */
    fun setRefreshToken(userId: UUID, refreshToken: String) {
        val key = AuthRedisKeys.refreshToken(userId)
        redisTemplate.opsForValue().set(key, refreshToken, 30, TimeUnit.DAYS)
    }
    
    /**
     * 리프레시 토큰 조회
     */
    fun getRefreshToken(userId: UUID): String? {
        return redisTemplate.opsForValue().get(AuthRedisKeys.refreshToken(userId))
    }
    
    /**
     * 리프레시 토큰 삭제
     */
    fun deleteRefreshToken(userId: UUID): Boolean {
        return redisTemplate.delete(AuthRedisKeys.refreshToken(userId))
    }
    
    // ================================
    // 3. 토큰 블랙리스트 관리
    // ================================
    
    /**
     * 토큰을 블랙리스트에 추가
     */
    fun addToBlacklist(token: String, reason: String, timeout: Long, timeUnit: TimeUnit) {
        val key = AuthRedisKeys.blacklistedToken(token)
        redisTemplate.opsForValue().set(key, reason, timeout, timeUnit)
    }
    
    /**
     * 토큰이 블랙리스트에 있는지 확인
     */
    fun isTokenBlacklisted(token: String): Boolean {
        return redisTemplate.hasKey(AuthRedisKeys.blacklistedToken(token))
    }
}