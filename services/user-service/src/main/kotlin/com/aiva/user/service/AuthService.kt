package com.aiva.user.service

import com.aiva.user.dto.*
import com.aiva.security.jwt.JwtUtil
import com.aiva.security.exception.*
import com.aiva.security.jwt.TokenType
import com.aiva.user.entity.User
import com.aiva.user.entity.Provider
import com.aiva.user.repository.UserRepository
import com.aiva.common.redis.auth.AuthRedisService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 인증 비즈니스 로직 서비스
 */
@Service
@Transactional
class AuthService(
    private val userRepository: UserRepository,
    private val jwtUtil: JwtUtil,
    private val authRedisService: AuthRedisService
) {
    
    /**
     * 앱에서 OAuth 완료 후 사용자 정보로 로그인 처리
     */
    fun authenticateFromApp(request: AppLoginRequest): AuthResponse {
        // 1. 기존 사용자 조회 또는 신규 생성
        val user = findOrCreateUser(request)

        // 2. 로그인 시간 업데이트
        user.updateLastLogin()
        
        // 3. Redis에 사용자 정보 캐시 (7일)
        authRedisService.setUserCache(user.id, user.email, user.nickname)
        
        // 4. JWT 토큰 생성
        val accessToken = jwtUtil.generateToken(user.id)
        val refreshToken = jwtUtil.generateRefreshToken(user.id)
        
        // 5. 리프레시 토큰 Redis에 저장 (30일)
        authRedisService.setRefreshToken(user.id, refreshToken)
        
        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtUtil.getExpirationTime(),
            user = UserInfo.from(user)
        )
    }
    
    /**
     * 리프레시 토큰으로 새 액세스 토큰 발급
     */
    fun refreshToken(refreshToken: String): AuthResponse {
        // 1. 리프레시 토큰 유효성 검증
        if (!jwtUtil.validateToken(refreshToken) || jwtUtil.getTokenType(refreshToken) != TokenType.REFRESH) {
            throw InvalidTokenException("유효하지 않은 리프레시 토큰입니다")
        }
        
        // 2. 사용자 ID 추출
        val userId = jwtUtil.getUserIdFromToken(refreshToken)
        
        // 3. Redis에서 저장된 리프레시 토큰과 비교
        val storedToken = authRedisService.getRefreshToken(userId)
        if (storedToken != refreshToken) {
            throw InvalidTokenException("리프레시 토큰이 일치하지 않습니다")
        }
        
        // 4. 사용자 정보 조회
        val user = userRepository.findById(userId)
            .orElseThrow { UnauthorizedException("사용자를 찾을 수 없습니다") }
        
        // 5. 로그인 시간 업데이트
        user.updateLastLogin()
        
        // 6. 새 토큰 발급
        val newAccessToken = jwtUtil.generateToken(user.id)
        val newRefreshToken = jwtUtil.generateRefreshToken(user.id)
        
        // 7. 새 리프레시 토큰 저장
        authRedisService.setRefreshToken(user.id, newRefreshToken)
        
        return AuthResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            expiresIn = jwtUtil.getExpirationTime(),
            user = UserInfo.from(user)
        )
    }
    
    /**
     * 로그아웃 처리 (토큰 무효화)
     */
    fun logout(authorization: String) {
        val accessToken = jwtUtil.extractTokenFromHeader(authorization)
            ?: throw InvalidTokenException("유효하지 않은 액세스 토큰입니다")
        
        val userId = jwtUtil.getUserIdFromToken(accessToken)
        
        // 1. 리프레시 토큰 삭제
        authRedisService.deleteRefreshToken(userId)
        
        // 2. 액세스 토큰 블랙리스트에 추가 (토큰 만료시까지)
        val remainingTime = jwtUtil.getClaimsFromToken(accessToken).expiration.time - System.currentTimeMillis()
        if (remainingTime > 0) {
            authRedisService.addToBlacklist(accessToken, "logout", remainingTime, TimeUnit.MILLISECONDS)
        }
    }
    
    /**
     * 현재 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    fun getCurrentUserInfo(userIdString: String): UserInfo {
        val userId = UUID.fromString(userIdString)
        val user = userRepository.findById(userId)
            .orElseThrow { UnauthorizedException("사용자를 찾을 수 없습니다") }
        
        return UserInfo.from(user)
    }
    
    /**
     * 앱에서 받은 사용자 정보로 기존 사용자 조회 또는 신규 생성
     */
    private fun findOrCreateUser(appUser: AppLoginRequest): User {
        val provider = Provider.valueOf(appUser.provider.uppercase())
        
        return userRepository.findByProviderAndProviderUserId(provider, appUser.providerUserId)
            ?: userRepository.save(
                User(
                    provider = provider,
                    providerUserId = appUser.providerUserId,
                    email = appUser.email,
                    nickname = appUser.nickname,
                    avatarUrl = appUser.avatarUrl
                )
            )
    }
}