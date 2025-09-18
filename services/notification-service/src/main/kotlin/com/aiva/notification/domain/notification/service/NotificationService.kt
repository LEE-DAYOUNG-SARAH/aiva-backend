package com.aiva.notification.service

import com.aiva.notification.dto.NotificationListResponse
import com.aiva.notification.dto.NotificationResponse
import com.aiva.notification.dto.ReadAllResponse
import com.aiva.notification.repository.NotificationRepository
import mu.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
) {
    private val logger = KotlinLogging.logger {}
    
    @Transactional(readOnly = true)
    fun getUserNotifications(
        userId: UUID,
        page: Int = 0,
        size: Int = 20
    ): NotificationListResponse {
        val oneMonthAgo = LocalDateTime.now().minusMonths(1)
        val pageable = PageRequest.of(page, size)
        
        val notificationsPage = notificationRepository.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
            userId, oneMonthAgo, pageable
        )
        
        val notificationResponses = notificationsPage.content.map { notification ->
            NotificationResponse(
                id = notification.id,
                type = notification.type,
                title = notification.title,
                body = notification.body,
                imageUrl = notification.imageUrl,
                linkUrl = notification.linkUrl,
                isRead = notification.isRead,
                readAt = notification.readAt,
                createdAt = notification.createdAt
            )
        }
        
        return NotificationListResponse(
            notifications = notificationResponses,
            totalCount = notificationRepository.countByUserIdAndCreatedAtAfter(userId, oneMonthAgo).toInt(),
            hasNext = notificationsPage.hasNext()
        )
    }
    
    @Transactional
    fun markAsRead(notificationId: UUID, userId: UUID) {
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { IllegalArgumentException("Notification not found: $notificationId") }
        
        if (notification.userId != userId) {
            throw IllegalArgumentException("User $userId is not authorized to read notification $notificationId")
        }
        
        notification.markAsRead()
    }
    
    @Transactional
    fun markAllAsRead(userId: UUID): ReadAllResponse {
        // 조회와 동일한 "최근 1개월" 로직 사용
        val oneMonthAgo = LocalDateTime.now().minusMonths(1)
        
        logger.info { "Processing read all for user $userId, marking notifications since: $oneMonthAgo" }
        
        val readAt = LocalDateTime.now()
        val readCount = notificationRepository.markAllAsRead(userId, readAt, oneMonthAgo)
        
        logger.info { "Read all completed: $readCount notifications marked as read for user $userId" }
        
        return ReadAllResponse(readCount = readCount)
    }
}