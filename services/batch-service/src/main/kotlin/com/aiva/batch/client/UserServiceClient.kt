package com.aiva.batch.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * User Service API 클라이언트 (하이브리드 방식의 API 호출용)
 */
@FeignClient(
    name = "user-service",
    url = "\${api.user-service.url:http://localhost:8081}"
)
interface UserServiceClient {
    
    /**
     * 사용자의 Pro 권한 상태 업데이트
     */
    @PutMapping("/api/users/{userId}/pro-status")
    fun updateProStatus(
        @PathVariable userId: UUID,
        @RequestBody request: UpdateProStatusRequest
    ): UserApiResponse
    
    /**
     * 사용자 정보 조회
     */
    @GetMapping("/api/users/{userId}")
    fun getUser(@PathVariable userId: UUID): UserDetailResponse
    
    /**
     * 사용자 활성 상태 업데이트
     */
    @PutMapping("/api/users/{userId}/active-status")
    fun updateActiveStatus(
        @PathVariable userId: UUID,
        @RequestBody request: UpdateActiveStatusRequest
    ): UserApiResponse
}

data class UpdateProStatusRequest(
    val isPro: Boolean,
    val reason: String? = null
)

data class UpdateActiveStatusRequest(
    val isActive: Boolean,
    val reason: String? = null
)

data class UserApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: Any? = null
)

data class UserDetailResponse(
    val id: UUID,
    val nickname: String,
    val email: String?,
    val isPro: Boolean,
    val proExpiresAt: String? = null
)