package com.aiva.user.user.controller

import com.aiva.common.response.ApiResponse
import com.aiva.user.user.dto.UserResponse
import com.aiva.user.user.service.UserReadService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 사용자 조회 관련 API 컨트롤러
 * 사용자 정보 조회, 검색 등을 담당
 */
@RestController
@RequestMapping("/api/users")
class UserReadController(
    private val userReadService: UserReadService
) {
    
    /**
     * 현재 사용자 정보 조회
     */
    @GetMapping("/me")
    fun getCurrentUser(@RequestHeader("X-User-Id") userId: String): ApiResponse<UserResponse> {
        return ApiResponse.success(
            userReadService.getUserInfo(userId)
        )
    }
}