package com.aiva.user.notification.dto

import com.aiva.user.notification.entity.NotificationPermissionType
import com.aiva.user.notification.entity.UserNotificationSetting
import java.time.LocalDateTime
import java.util.*

data class NotificationSettingUpdateRequest(
    val isEnabled: Boolean
)

data class NotificationSettingUpdateResponse(
    val id: UUID,
    val permissionType: NotificationPermissionType,
    val isEnabled: Boolean,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(setting: UserNotificationSetting) = NotificationSettingUpdateResponse(
            id = setting.id,
            permissionType = setting.permissionType,
            isEnabled = setting.isEnabled,
            updatedAt = setting.updatedAt
        )
    }
}

data class NotificationPermissionResponse(
    val id: UUID,
    val permissionType: NotificationPermissionType,
    val isEnabled: Boolean
) {
    companion object {
        fun from(setting: UserNotificationSetting) = NotificationPermissionResponse(
            id = setting.id,
            permissionType = setting.permissionType,
            isEnabled = setting.isEnabled
        )
    }
}
