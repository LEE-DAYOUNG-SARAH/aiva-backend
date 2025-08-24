package com.aiva.security.exception

/**
 * 인증/인가 관련 예외 클래스들
 */

class InvalidTokenException(message: String) : RuntimeException(message)

class ExpiredTokenException(message: String) : RuntimeException(message)

class UnauthorizedException(message: String) : RuntimeException(message)

class ForbiddenException(message: String) : RuntimeException(message)

class OAuthException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)