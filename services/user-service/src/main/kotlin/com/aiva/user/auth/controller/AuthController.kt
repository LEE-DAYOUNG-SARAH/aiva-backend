package com.aiva.user.auth.controller

import com.aiva.user.auth.service.AuthService
import com.aiva.common.response.ApiResponse
import com.aiva.user.auth.dto.AppLoginRequest
import com.aiva.user.auth.dto.AppLoginResponse
import com.aiva.user.auth.dto.AuthResponse
import com.aiva.user.auth.dto.RefreshTokenRequest
import org.springframework.web.bind.annotation.*

/**
 * 인증 관련 API 컨트롤러
 * JWT 토큰 발급, 갱신, 로그아웃
 */
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    
    /**
     * 앱 로그인 (카카오/구글)
     * 앱에서 OAuth 완료 후 사용자 정보로 로그인 처리
     */
    @PostMapping("/login")
    fun login(@RequestBody request: AppLoginRequest): ApiResponse<AppLoginResponse> {
        return ApiResponse.success(
            authService.login(request)
        )
    }
    
    /**
     * 리프레시 토큰으로 새 액세스 토큰 발급
     */
    @PostMapping("/refresh")
    fun refresh(@RequestBody request: RefreshTokenRequest): ApiResponse<AuthResponse> {
        return ApiResponse.success(
            authService.refreshToken(request.refreshToken)
        )
    }
    
    /**
     * 로그아웃 (토큰 무효화)
     */
    @PostMapping("/logout")
    fun logout(
        @RequestHeader("Authorization") authorization: String
    ): ApiResponse<Unit> {
        authService.logout(authorization)
        return ApiResponse.success()
    }
}