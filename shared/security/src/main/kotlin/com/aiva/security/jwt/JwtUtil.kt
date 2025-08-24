package com.aiva.security.jwt

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*
import javax.crypto.SecretKey

/**
 * JWT 토큰 생성/검증 유틸리티
 * 모든 서비스에서 공통으로 사용
 */
@Component
class JwtUtil(
    @Value("\${jwt.secret}") 
    private val jwtSecret: String,
    
    @Value("\${jwt.expiration}") 
    private val jwtExpiration: Long
) {
    
    private val key: SecretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    
    /**
     * JWT 토큰 생성 (UUID만 포함)
     */
    fun generateToken(userId: UUID): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpiration)
        
        return Jwts.builder()
            .subject(userId.toString())
            .claim("type", TokenType.ACCESS.name)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }
    
    /**
     * 리프레시 토큰 생성 (30일)
     */
    fun generateRefreshToken(userId: UUID): String {
        val now = Date()
        val expiryDate = Date(now.time + (30 * 24 * 60 * 60 * 1000L)) // 30일
        
        return Jwts.builder()
            .subject(userId.toString())
            .claim("type", TokenType.REFRESH.name)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }
    
    /**
     * 토큰에서 사용자 ID 추출
     */
    fun getUserIdFromToken(token: String): UUID {
        val claims = getClaimsFromToken(token)
        return UUID.fromString(claims.subject)
    }
    
    /**
     * 토큰에서 만료 시간 추출
     */
    fun getExpirationDateFromToken(token: String): LocalDateTime? {
        return try {
            val claims = getClaimsFromToken(token)
            claims.expiration?.toInstant()
                ?.atZone(java.time.ZoneId.systemDefault())
                ?.toLocalDateTime()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 토큰 유효성 검증
     */
    fun validateToken(token: String): Boolean {
        return try {
            val claims = getClaimsFromToken(token)
            !claims.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 토큰 타입 확인 (ACCESS/REFRESH)
     */
    fun getTokenType(token: String): TokenType? {
        return try {
            val claims = getClaimsFromToken(token)
            val typeString = claims["type"] as String?
            typeString?.let { TokenType.valueOf(it) }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 토큰이 블랙리스트에 있는지 확인 (Gateway에서 Redis 접근 필요 시 사용)
     */
    fun isTokenBlacklisted(token: String): Boolean {
        // 이 메서드는 AuthService에서 구현되어 있음
        // Gateway에서는 직접 Redis를 확인할 수 있도록 별도 구현 필요
        return false
    }
    
    /**
     * Bearer 토큰에서 실제 토큰 추출
     */
    fun extractTokenFromHeader(bearerToken: String?): String? {
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }
    
    /**
     * 토큰에서 Claims 추출
     */
    fun getClaimsFromToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    /**
     * 만료시간 조회(초)
     */
     fun getExpirationTime() = jwtExpiration / 1000
}