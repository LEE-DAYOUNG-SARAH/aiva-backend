package com.aiva.notification.consumer.persistence

import com.aiva.notification.domain.notification.dto.CommunityNotificationEvent
import com.aiva.notification.domain.notification.entity.Notification
import com.aiva.notification.domain.notification.repository.NotificationRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

/**
 * 알림 영속성 처리 담당 서비스
 * 
 * 단일 책임: 알림 데이터의 데이터베이스 저장/조회
 */
@Service
class NotificationPersistenceService(
    private val notificationRepository: NotificationRepository
) {
    
    private val logger = KotlinLogging.logger {}
    
    /**
     * 커뮤니티 알림 이벤트를 데이터베이스에 저장
     * 
     * @param event 저장할 알림 이벤트
     * @return 저장된 알림 목록
     */
    @Transactional
    fun saveNotifications(event: CommunityNotificationEvent): List<Notification> {
        logger.debug { "Saving notifications for ${event.targetUserIds.size} users" }
        
        val notifications = event.targetUserIds.map { userId ->
            createNotificationFromEvent(event, userId)
        }
        
        val savedNotifications = try {
            notificationRepository.saveAll(notifications)
        } catch (e: Exception) {
            logger.error(e) { "Failed to save notifications for users: ${event.targetUserIds}" }
            throw NotificationPersistenceException("Failed to save notifications", event.targetUserIds, e)
        }
        
        logger.info { "Successfully saved ${savedNotifications.size} notifications" }
        return savedNotifications
    }
    
    private fun createNotificationFromEvent(
        event: CommunityNotificationEvent, 
        userId: UUID
    ): Notification {
        return Notification(
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
    
    /**
     * 알림 영속성 처리 전용 예외 클래스
     */
    class NotificationPersistenceException(
        message: String,
        val targetUserIds: List<UUID>,
        cause: Throwable
    ) : RuntimeException(message, cause)
}