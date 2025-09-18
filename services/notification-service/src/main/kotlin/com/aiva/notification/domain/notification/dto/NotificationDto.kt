package com.aiva.notification.domain.notification.dto

import com.aiva.notification.domain.notification.entity.NotificationType
import java.time.LocalDateTime
import java.util.*

data class NotificationResponse(
    val id: UUID,
    val type: NotificationType,
    val title: String,
    val body: String,
    val imageUrl: String?,
    val linkUrl: String?,
    val isRead: Boolean,
    val readAt: LocalDateTime?,
    val createdAt: LocalDateTime
)

data class NotificationListResponse(
    val notifications: List<NotificationResponse>,
    val totalCount: Int,
    val hasNext: Boolean
)

data class NotificationSendRequest(
    val type: NotificationType,
    val title: String,
    val body: String,
    val imageUrl: String? = null,
    val linkUrl: String? = null,
    val targetUserIds: List<UUID>,
    val scheduledAt: LocalDateTime? = null
)

data class CommunityNotificationEvent(
    val notificationId: UUID,
    val type: NotificationType,
    val title: String,
    val body: String,
    val imageUrl: String? = null,
    val linkUrl: String? = null,
    val targetUserIds: List<UUID>
)