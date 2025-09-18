package com.aiva.notification.domain.device.controller

import com.aiva.common.response.ApiResponse
import com.aiva.notification.domain.device.dto.DeviceUpdateResponse
import com.aiva.notification.domain.device.dto.DeviceUpdateRequest
import com.aiva.notification.domain.device.service.DeviceService
import com.aiva.security.annotation.CurrentUser
import com.aiva.security.dto.UserPrincipal
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/devices")
@PreAuthorize("hasRole('USER')")
class DeviceController(
    private val deviceService: DeviceService
) {
    
    /**
     * 디바이스 상세 정보 업데이트
     */
    @PutMapping("/{deviceIdentifier}")
    fun updateDeviceDetails(
        @CurrentUser userPrincipal: UserPrincipal,
        @PathVariable deviceIdentifier: String,
        @Valid @RequestBody request: DeviceUpdateRequest
    ): ApiResponse<DeviceUpdateResponse> {
        val device = deviceService.updateDeviceDetails(
            userPrincipal.userId, 
            deviceIdentifier, 
            request
        )
        return ApiResponse.success(device)
    }
}