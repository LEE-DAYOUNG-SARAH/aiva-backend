package com.aiva.user.device.controller

import com.aiva.common.response.ApiResponse
import com.aiva.user.device.dto.DeviceResponse
import com.aiva.user.device.dto.DeviceUpdateRequest
import com.aiva.user.device.service.DeviceService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/users/devices")
class DeviceController(
    private val deviceService: DeviceService
) {
    
    /**
     * 디바이스 상세 정보 업데이트
     */
    @PutMapping("/{deviceIdentifier}")
    fun updateDeviceDetails(
        @RequestHeader("X-User-Id") userId: String,
        @PathVariable deviceIdentifier: String,
        @Valid @RequestBody request: DeviceUpdateRequest
    ): ApiResponse<DeviceResponse> {
        val device = deviceService.updateDeviceDetails(
            UUID.fromString(userId), 
            deviceIdentifier, 
            request
        )
        return ApiResponse.success(device)
    }
}
