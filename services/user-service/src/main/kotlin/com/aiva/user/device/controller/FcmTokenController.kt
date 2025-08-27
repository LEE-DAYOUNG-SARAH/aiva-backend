package com.aiva.user.device.controller

import com.aiva.common.response.ApiResponse
import com.aiva.user.device.dto.FcmTokenResponse
import com.aiva.user.device.dto.FcmTokenUpdateRequest
import com.aiva.user.device.service.FcmTokenService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/users/devices")
class FcmTokenController(
    private val fcmTokenService: FcmTokenService
) {
    
    /**
     * FCM 토큰 업데이트
     */
    @PutMapping("/{deviceIdentifier}/fcm-token")
    fun updateFcmToken(
        @RequestHeader("X-User-Id") userId: String,
        @PathVariable deviceIdentifier: String,
        @Valid @RequestBody request: FcmTokenUpdateRequest
    ): ApiResponse<FcmTokenResponse> {
        val fcmToken = fcmTokenService.updateFcmToken(
            UUID.fromString(userId), 
            deviceIdentifier, 
            request
        )
        return ApiResponse.success(fcmToken)
    }
}
