package com.aiva.notification.consumer

import com.aiva.notification.domain.notification.dto.CommunityNotificationEvent
import com.aiva.notification.domain.notification.entity.Notification
import com.aiva.notification.domain.notification.entity.NotificationRecipient
import com.aiva.notification.domain.notification.repository.NotificationRepository
import com.aiva.notification.domain.notification.repository.NotificationRecipientRepository
import com.aiva.notification.domain.notification.service.FcmService
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
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
    fun handleCommunityNotification(message: String) {
        val event = runCatching {
            logger.info { "Received community notification: $message" }
            objectMapper.readValue(message, CommunityNotificationEvent::class.java)
        }.getOrElse { e ->
            logger.error(e) { "Failed to parse community notification: $message" }
            throw e
        }
        
        // 알림 저장
        val savedNotifications = saveNotifications(event)
        
        // FCM 발송 (notificationId 포함)
        sendFcmNotifications(event, savedNotifications)
    }
    
    private fun saveNotifications(event: CommunityNotificationEvent): List<Notification> = runCatching {
        val notifications = event.targetUserIds.map { userId ->
            Notification(
                id = UUID.randomUUID(),
                userId = userId,
                type = event.type,
                title = event.title,
                body = event.body,
                imageUrl = event.imageUrl,
                linkUrl = event.linkUrl,
                isRead = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        }
        
        val saved = notificationRepository.saveAll(notifications)
        logger.info { "Saved ${saved.size} notifications for users: ${event.targetUserIds}" }
        saved
    }.onFailure { e ->
        logger.error(e) { "Failed to save notifications for users: ${event.targetUserIds}" }
        throw e
    }.getOrThrow()
    
    private fun sendFcmNotifications(event: CommunityNotificationEvent, savedNotifications: List<Notification>) = runCatching {
        val fcmTokens = fcmTokenService.getActiveFcmTokensByUserIds(event.targetUserIds)
        
        if (fcmTokens.isEmpty()) {
            logger.warn { "No FCM tokens found for users: ${event.targetUserIds}" }
            return@runCatching
        }
        
        logger.info { "Found ${fcmTokens.size} FCM tokens for ${event.targetUserIds.size} users" }
        
        // userId별 notificationId 매핑
        val userNotificationMap = savedNotifications.associateBy { it.userId }
        
        // 각 FCM 토큰별로 NotificationRecipient 생성 및 FCM 발송
        val recipients = fcmTokens.mapNotNull { fcmToken ->
            val notification = userNotificationMap[fcmToken.userId] ?: return@mapNotNull null
            
            runCatching {
                // NotificationRecipient 생성
                val recipient = NotificationRecipient(
                    notificationId = notification.id,
                    userId = fcmToken.userId
                )
                
                // FCM 발송
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
                
                recipient
            }.onFailure { e ->
                logger.warn(e) { "Failed to send FCM to token: ${fcmToken.token}, user: ${fcmToken.userId}" }
            }.getOrNull()
        }
        
        // NotificationRecipient 일괄 저장
        notificationRecipientRepository.saveAll(recipients)
        
        logger.info { "Sent FCM notifications with notificationIds for ${fcmTokens.size} tokens" }
        
    }.onFailure { e ->
        logger.error(e) { "Error sending FCM notifications for community event" }
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