package com.aiva.notification.domain.setting.controller

import com.aiva.common.response.ApiResponse
import com.aiva.notification.domain.setting.dto.NotificationPermissionResponse
import com.aiva.notification.domain.setting.dto.NotificationSettingUpdateRequest
import com.aiva.notification.domain.setting.dto.NotificationSettingUpdateResponse
import com.aiva.notification.domain.setting.service.NotificationSettingService
import com.aiva.security.annotation.CurrentUser
import com.aiva.security.dto.UserPrincipal
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/users/notification-settings")
@PreAuthorize("hasRole('USER')")
class NotificationSettingController(
    private val notificationSettingService: NotificationSettingService
) {
    
    /**
     * 사용자 알림 설정 조회
     */
    @GetMapping
    fun getNotificationSettings(
        @CurrentUser userPrincipal: UserPrincipal
    ): ApiResponse<List<NotificationPermissionResponse>> {
        return ApiResponse.success(
            notificationSettingService.getNotificationSettings(userPrincipal.userId)
        )
    }
    
    /**
     * 사용자 알림 설정 수정
     */
    @PutMapping("/{settingId}")
    fun updateNotificationSetting(
        @CurrentUser userPrincipal: UserPrincipal,
        @PathVariable settingId: UUID,
        @RequestBody request: NotificationSettingUpdateRequest
    ): ApiResponse<NotificationSettingUpdateResponse> {
        val updatedSettings = notificationSettingService.updateNotificationSetting(
            userPrincipal.userId,
            settingId,
            request
        )
        return ApiResponse.success(updatedSettings)
    }
}