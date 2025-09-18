package com.aiva.notification.device.controller

import com.aiva.common.response.ApiResponse
import com.aiva.notification.device.dto.FcmTokenUpdateResponse
import com.aiva.notification.device.dto.FcmTokenUpdateRequest
import com.aiva.notification.device.service.DeviceFcmTokenService
import com.aiva.security.annotation.CurrentUser
import com.aiva.security.dto.UserPrincipal
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/devices")
@PreAuthorize("hasRole('USER')")
class FcmTokenController(
    private val fcmTokenService: DeviceFcmTokenService
) {
    
    /**
     * FCM 토큰 업데이트
     */
    @PutMapping("/{deviceIdentifier}/fcm-token")
    fun updateFcmToken(
        @CurrentUser userPrincipal: UserPrincipal,
        @PathVariable deviceIdentifier: String,
        @Valid @RequestBody request: FcmTokenUpdateRequest
    ): ApiResponse<FcmTokenUpdateResponse> {
        val fcmToken = fcmTokenService.updateFcmToken(
            userPrincipal.userId, 
            deviceIdentifier, 
            request
        )
        return ApiResponse.success(fcmToken)
    }
}