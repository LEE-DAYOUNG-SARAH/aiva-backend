package com.aiva.user.notification.controller

import com.aiva.common.response.ApiResponse
import com.aiva.user.notification.dto.*
import com.aiva.user.notification.service.NotificationSettingService
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/users/notification-settings")
class NotificationSettingController(
    private val notificationSettingService: NotificationSettingService
) {
    
    /**
     * 사용자 알림 설정 조회
     */
    @GetMapping
    fun getNotificationSettings(
        @RequestHeader("X-User-Id") userId: String
    ): ApiResponse<List<NotificationPermissionResponse>> {
        return ApiResponse.success(
            notificationSettingService.getNotificationSettings(UUID.fromString(userId))
        )
    }
    
    /**
     * 사용자 알림 설정 수정
     */
    @PutMapping("/{settingId}")
    fun updateNotificationSetting(
        @RequestHeader("X-User-Id") userId: String,
        @PathVariable settingId: String,
        @RequestBody request: NotificationSettingUpdateRequest
    ): ApiResponse<NotificationSettingUpdateResponse> {
        val updatedSettings = notificationSettingService.updateNotificationSetting(
            UUID.fromString(userId),
            UUID.fromString(settingId),
            request
        )
        return ApiResponse.success(updatedSettings)
    }
}
