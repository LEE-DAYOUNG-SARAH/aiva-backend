package com.aiva.security.jwt

/**
 * JWT 토큰 타입
 */
enum class TokenType {
    ACCESS,   // API 접근용 토큰 (24시간)
    REFRESH   // 토큰 갱신용 (30일)
}