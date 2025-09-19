package com.aiva.notification.consumer.delivery

import com.aiva.notification.consumer.FcmTokenService
import com.aiva.notification.consumer.FcmTokenDto
import com.aiva.notification.domain.notification.dto.CommunityNotificationEvent
import com.aiva.notification.domain.notification.entity.Notification
import com.aiva.notification.domain.notification.entity.NotificationRecipient
import com.aiva.notification.domain.notification.repository.NotificationRecipientRepository
import com.aiva.notification.domain.notification.service.FcmService
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 알림 전송 담당 서비스
 * 
 * 단일 책임: FCM을 통한 알림 전송 및 전송 결과 관리
 */
@Service
class NotificationDeliveryService(
    private val fcmService: FcmService,
    private val fcmTokenService: FcmTokenService,
    private val notificationRecipientRepository: NotificationRecipientRepository
) {
    
    private val logger = KotlinLogging.logger {}
    
    /**
     * 저장된 알림을 FCM을 통해 전송
     * 
     * @param event 원본 이벤트 (FCM 메시지 구성용)
     * @param savedNotifications 전송할 알림 목록
     */
    @Transactional
    fun deliverNotifications(
        event: CommunityNotificationEvent, 
        savedNotifications: List<Notification>
    ) {
        logger.debug { "Starting notification delivery for ${savedNotifications.size} notifications" }
        
        val fcmTokens = fcmTokenService.getActiveFcmTokensByUserIds(event.targetUserIds)
        
        if (fcmTokens.isEmpty()) {
            logger.warn { "No FCM tokens found for users: ${event.targetUserIds}" }
            return
        }
        
        logger.info { "Found ${fcmTokens.size} FCM tokens for ${event.targetUserIds.size} users" }
        
        val userNotificationMap = savedNotifications.associateBy { it.userId }
        val deliveryResults = deliverToFcmTokens(event, fcmTokens, userNotificationMap)
        
        // 성공한 전송 결과만 저장
        val successfulRecipients = deliveryResults.mapNotNull { it.recipient }
        if (successfulRecipients.isNotEmpty()) {
            notificationRecipientRepository.saveAll(successfulRecipients)
            logger.info { "Saved ${successfulRecipients.size} notification recipients" }
        }
        
        logDeliveryResults(deliveryResults)
    }
    
    private fun deliverToFcmTokens(
        event: CommunityNotificationEvent,
        fcmTokens: List<FcmTokenDto>,
        userNotificationMap: Map<UUID, Notification>
    ): List<DeliveryResult> {
        return fcmTokens.map { fcmToken ->
            val notification = userNotificationMap[fcmToken.userId]
            if (notification == null) {
                DeliveryResult(
                    success = false,
                    error = "Notification not found for user: ${fcmToken.userId}",
                    recipient = null
                )
            } else {
                deliverSingleNotification(event, fcmToken, notification)
            }
        }
    }
    
    private fun deliverSingleNotification(
        event: CommunityNotificationEvent,
        fcmToken: FcmTokenDto,
        notification: Notification
    ): DeliveryResult {
        return try {
            fcmService.sendNotification(
                fcmToken = fcmToken.token,
                title = event.title,
                body = event.body,
                imageUrl = event.imageUrl,
                linkUrl = event.linkUrl,
                data = mapOf(
                    "type" to event.type.name,
                    "notificationId" to notification.id.toString()
                )
            )
            
            val recipient = NotificationRecipient(
                notificationId = notification.id,
                userId = fcmToken.userId
            )
            
            DeliveryResult(success = true, error = null, recipient = recipient)
            
        } catch (e: Exception) {
            logger.warn(e) { "Failed to send FCM to token: ${fcmToken.token}, user: ${fcmToken.userId}" }
            DeliveryResult(
                success = false,
                error = e.message ?: "Unknown FCM delivery error",
                recipient = null
            )
        }
    }
    
    private fun logDeliveryResults(results: List<DeliveryResult>) {
        val successCount = results.count { it.success }
        val failureCount = results.size - successCount
        
        logger.info { "Notification delivery completed. Success: $successCount, Failed: $failureCount" }
        
        if (failureCount > 0) {
            val errors = results.filter { !it.success }.map { it.error }
            logger.warn { "Delivery failures: $errors" }
        }
    }
    
    /**
     * 전송 결과를 담는 데이터 클래스
     */
    private data class DeliveryResult(
        val success: Boolean,
        val error: String?,
        val recipient: NotificationRecipient?
    )
}