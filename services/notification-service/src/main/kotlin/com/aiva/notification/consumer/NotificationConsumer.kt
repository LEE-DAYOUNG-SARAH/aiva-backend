package com.aiva.notification.consumer

import com.aiva.notification.domain.notification.dto.CommunityNotificationEvent
import com.aiva.notification.domain.notification.entity.Notification
import com.aiva.notification.domain.notification.entity.NotificationRecipient
import com.aiva.notification.domain.notification.repository.NotificationRepository
import com.aiva.notification.domain.notification.repository.NotificationRecipientRepository
import com.aiva.notification.domain.notification.service.FcmService
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
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
    fun handleCommunityNotification(message: String) = runBlocking {
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
    
    private suspend fun sendFcmNotifications(event: CommunityNotificationEvent, savedNotifications: List<Notification>) = runCatching {
        val fcmTokens = fcmTokenService.getActiveFcmTokensByUserIds(event.targetUserIds)
        
        if (fcmTokens.isEmpty()) {
            logger.warn { "No FCM tokens found for users: ${event.targetUserIds}" }
            return@runCatching
        }
        
        logger.info { "Found ${fcmTokens.size} FCM tokens for ${event.targetUserIds.size} users" }
        
        // userId별 notificationId 매핑
        val userNotificationMap = savedNotifications.associateBy { it.userId }
        
        // 모든 FCM 토큰을 일괄 처리
        val tokens = fcmTokens.map { it.token }
        
        try {
            // 코루틴 기반 배치 발송
            fcmService.sendBatchNotifications(
                tokens = tokens,
                title = event.title,
                body = event.body,
                imageUrl = event.imageUrl,
                linkUrl = event.linkUrl,
                data = mapOf(
                    "type" to event.type.name
                )
            )
            
            // 성공한 토큰에 대한 NotificationRecipient 생성
            val recipients = fcmTokens.mapNotNull { fcmToken ->
                val notification = userNotificationMap[fcmToken.userId] ?: return@mapNotNull null
                NotificationRecipient(
                    notificationId = notification.id,
                    userId = fcmToken.userId
                )
            }
            
            // NotificationRecipient 일괄 저장
            notificationRecipientRepository.saveAll(recipients)
            
            logger.info { "Completed FCM batch notification for ${tokens.size} tokens" }
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to send FCM batch notifications" }
            throw e
        }
        
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