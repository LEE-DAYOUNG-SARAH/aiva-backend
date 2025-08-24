package com.aiva.batch.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * Notification Service API 클라이언트 (하이브리드 방식의 API 호출용)
 */
@FeignClient(
    name = "notification-service",
    url = "\${api.notification-service.url:http://localhost:8084}"
)
interface NotificationServiceClient {
    
    /**
     * 구독 만료 예정 알림 발송
     */
    @PostMapping("/api/notifications/subscription-expiring")
    fun sendSubscriptionExpiringNotification(
        @RequestBody request: SubscriptionExpiringNotificationRequest
    ): NotificationApiResponse
    
    /**
     * 구독 만료 알림 발송
     */
    @PostMapping("/api/notifications/subscription-expired")
    fun sendSubscriptionExpiredNotification(
        @RequestBody request: SubscriptionExpiredNotificationRequest
    ): NotificationApiResponse
    
    /**
     * 구독 갱신 실패 알림 발송
     */
    @PostMapping("/api/notifications/subscription-renewal-failed")
    fun sendSubscriptionRenewalFailedNotification(
        @RequestBody request: SubscriptionRenewalFailedNotificationRequest
    ): NotificationApiResponse
    
    /**
     * 예약 알림 발송
     */
    @PostMapping("/api/notifications/{notificationId}/send")
    fun sendScheduledNotification(
        @PathVariable notificationId: UUID,
        @RequestBody request: SendScheduledNotificationRequest
    ): NotificationApiResponse
    
    /**
     * 대량 알림 발송
     */
    @PostMapping("/api/notifications/bulk-send")
    fun sendBulkNotifications(
        @RequestBody request: BulkNotificationRequest
    ): NotificationApiResponse
}

data class SubscriptionExpiringNotificationRequest(
    val userId: UUID,
    val subscriptionId: UUID,
    val expiresAt: String,
    val daysUntilExpiry: Int
)

data class SubscriptionExpiredNotificationRequest(
    val userId: UUID,
    val subscriptionId: UUID,
    val expiredAt: String
)

data class SubscriptionRenewalFailedNotificationRequest(
    val userId: UUID,
    val subscriptionId: UUID,
    val failureReason: String,
    val nextRetryAt: String?
)

data class SendScheduledNotificationRequest(
    val targetUserIds: List<UUID>? = null,
    val sendImmediately: Boolean = true
)

data class BulkNotificationRequest(
    val notificationId: UUID,
    val targetUserIds: List<UUID>,
    val options: BulkNotificationOptions? = null
)

data class BulkNotificationOptions(
    val batchSize: Int = 100,
    val delayBetweenBatches: Long = 1000L
)

data class NotificationApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: Any? = null
)