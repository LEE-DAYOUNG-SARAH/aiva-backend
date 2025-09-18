package com.aiva.notification.consumer

import com.aiva.notification.dto.CommunityNotificationEvent
import com.aiva.notification.entity.Notification
import com.aiva.notification.entity.NotificationRecipient
import com.aiva.notification.repository.NotificationRepository
import com.aiva.notification.repository.NotificationRecipientRepository
import com.aiva.notification.service.FcmService
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Component
class NotificationConsumer(
    private val notificationRepository: NotificationRepository,
    private val notificationRecipientRepository: NotificationRecipientRepository,
    private val fcmService: FcmService,
    private val objectMapper: ObjectMapper,
    private val fcmTokenService: FcmTokenService
) {
    
    private val logger = KotlinLogging.logger {}
    
    @KafkaListener(
        topics = ["community.notification"], 
        groupId = "notification-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    fun handleCommunityNotification(message: String) {
        try {
            logger.info { "Received community notification: $message" }
            
            val event = objectMapper.readValue(message, CommunityNotificationEvent::class.java)
            
            // 1. 알림 저장
            val notification = Notification(
                id = event.notificationId,
                type = event.type,
                title = event.title,
                body = event.body,
                imageUrl = event.imageUrl,
                linkUrl = event.linkUrl,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            
            val savedNotification = notificationRepository.save(notification)
            logger.info { "Saved notification: ${savedNotification.id}" }
            
            // 2. 수신자 저장
            val recipients = event.targetUserIds.map { userId ->
                NotificationRecipient(
                    notificationId = savedNotification.id,
                    userId = userId,
                    isRead = false,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
            }
            
            notificationRecipientRepository.saveAll(recipients)
            logger.info { "Saved ${recipients.size} notification recipients" }
            
            // 3. FCM 토큰 조회 및 발송
            sendFcmNotifications(event, savedNotification.id)
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to process community notification: $message" }
            throw e // 재시도를 위해 예외를 다시 던짐
        }
    }
    
    private fun sendFcmNotifications(event: CommunityNotificationEvent, notificationId: UUID) {
        try {
            // FCM 토큰 조회
            val fcmTokens = fcmTokenService.getActiveFcmTokensByUserIds(event.targetUserIds)
            
            if (fcmTokens.isEmpty()) {
                logger.warn { "No FCM tokens found for users: ${event.targetUserIds}" }
                return
            }
            
            logger.info { "Found ${fcmTokens.size} FCM tokens for notification ${notificationId}" }
            
            // 배치 발송
            val additionalData = mapOf(
                "notificationId" to notificationId.toString(),
                "type" to event.type.name
            )
            
            fcmService.sendBatchNotifications(
                tokens = fcmTokens.map { it.token },
                title = event.title,
                body = event.body,
                imageUrl = event.imageUrl,
                linkUrl = event.linkUrl,
                data = additionalData
            ).whenComplete { results, exception ->
                if (exception != null) {
                    logger.error(exception) { "Failed to send FCM notifications for notification $notificationId" }
                } else {
                    logger.info { "Successfully sent ${results.size} FCM notifications for notification $notificationId" }
                }
            }
            
        } catch (e: Exception) {
            logger.error(e) { "Error sending FCM notifications for notification $notificationId" }
        }
    }
}

// FCM 토큰 서비스를 위한 간단한 인터페이스 (실제 구현은 user-service와 연동 필요)
interface FcmTokenService {
    fun getActiveFcmTokensByUserIds(userIds: List<UUID>): List<FcmTokenDto>
}

data class FcmTokenDto(
    val userId: UUID,
    val token: String,
    val deviceId: String,
    val isActive: Boolean
)