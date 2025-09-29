package com.aiva.gateway.filter

import com.aiva.security.jwt.JwtUtil
import com.aiva.security.jwt.TokenType
import com.aiva.security.exception.UnauthorizedException
import com.aiva.common.redis.auth.AuthRedisService
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * JWT 인증 필터
 * 모든 요청에 대해 JWT 토큰 검증 (예외 경로 제외)
 */
@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil,
    private val authRedisService: AuthRedisService
) {

    fun apply(): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val request = exchange.request
            val path = request.uri.path
            
            // 인증이 필요 없는 경로들
            if (isPublicPath(path)) {
                return@GatewayFilter chain.filter(exchange)
            }
            
            // JWT 토큰 검증
            val token = extractToken(request)
            if (token == null || !jwtUtil.validateToken(token)) {
                return@GatewayFilter handleUnauthorized(exchange, "유효하지 않거나 만료된 토큰입니다")
            }
            
            // ACCESS 토큰만 허용 (REFRESH 토큰으로는 API 접근 불가)
            if (jwtUtil.getTokenType(token) != TokenType.ACCESS) {
                return@GatewayFilter handleUnauthorized(exchange, "API 접근에는 ACCESS 토큰이 필요합니다")
            }
            
            // 블랙리스트 토큰 확인
            if (authRedisService.isTokenBlacklisted(token)) {
                return@GatewayFilter handleUnauthorized(exchange, "로그아웃되었거나 유효하지 않은 토큰입니다")
            }
            
            // 사용자 정보를 헤더에 추가
            val userId = jwtUtil.getUserIdFromToken(token)
            val nickname = jwtUtil.getNicknameFromToken(token)
            val profileUrl = jwtUtil.getProfileUrlFromToken(token)
            
            val modifiedRequest = request.mutate()
                .header("X-User-Id", userId.toString())
                .apply { 
                    if (nickname != null) header("X-User-Nickname", nickname)
                    if (profileUrl != null) header("X-User-Profile-Url", profileUrl)
                }
                .build()
            val modifiedExchange = exchange.mutate().request(modifiedRequest).build()
            
            chain.filter(modifiedExchange)
        }
    }
    
    /**
     * 인증이 필요 없는 공개 경로인지 확인
     */
    private fun isPublicPath(path: String): Boolean {
        val publicPaths = listOf(
            "/api/auth/login",              // 앱 로그인
            "/api/auth/refresh",            // 토큰 갱신
            "/actuator/health",             // 헬스체크
            "/actuator/info",               // 서비스 정보
            "/api-docs",                    // API 문서
            "/swagger-ui"                   // Swagger UI
        )
        
        return publicPaths.any { path.startsWith(it) }
    }
    
    /**
     * 요청에서 JWT 토큰 추출
     */
    private fun extractToken(request: ServerHttpRequest): String? {
        val authHeader = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        return jwtUtil.extractTokenFromHeader(authHeader)
    }
    
    /**
     * 인증 실패 응답 처리
     */
    private fun handleUnauthorized(exchange: ServerWebExchange, message: String = "인증이 필요합니다. 로그인 후 다시 시도해주세요."): Mono<Void> {
        val response: ServerHttpResponse = exchange.response
        response.statusCode = HttpStatus.UNAUTHORIZED
        response.headers.add("Content-Type", "application/json")
        
        val errorBody = """
            {
                "error": "UNAUTHORIZED",
                "message": "$message",
                "timestamp": "${System.currentTimeMillis()}",
                "path": "${exchange.request.uri.path}"
            }
        """.trimIndent()
        
        val buffer = response.bufferFactory().wrap(errorBody.toByteArray())
        return response.writeWith(Mono.just(buffer))
    }
    

}