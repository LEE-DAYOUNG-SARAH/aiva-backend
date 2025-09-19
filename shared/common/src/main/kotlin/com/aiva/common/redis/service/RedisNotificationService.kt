package com.aiva.common.redis.service

import com.aiva.common.redis.entity.NotificationQueue
import com.aiva.common.redis.repository.NotificationQueueRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class RedisNotificationService(
    private val notificationQueueRepository: NotificationQueueRepository
) {
    
    fun enqueueNotification(
        notificationId: UUID,
        userId: UUID,
        type: NotificationQueue.NotificationType,
        title: String,
        body: String,
        imageUrl: String? = null,
        linkUrl: String? = null,
        payload: Map<String, Any> = emptyMap(),
        priority: Int = 5,
        scheduledAt: LocalDateTime? = null
    ): NotificationQueue {
        val queueId = UUID.randomUUID().toString()
        val status = if (scheduledAt != null && scheduledAt.isAfter(LocalDateTime.now())) {
            NotificationQueue.QueueStatus.SCHEDULED
        } else {
            NotificationQueue.QueueStatus.PENDING
        }
        
        val notification = NotificationQueue(
            queueId = queueId,
            notificationId = notificationId,
            userId = userId,
            type = type,
            title = title,
            body = body,
            imageUrl = imageUrl,
            linkUrl = linkUrl,
            payload = payload,
            status = status,
            priority = priority,
            scheduledAt = scheduledAt
        )
        return notificationQueueRepository.save(notification)
    }
    
    fun getQueuedNotification(queueId: String): NotificationQueue? {
        return notificationQueueRepository.findById(queueId).orElse(null)
    }
    
    fun getPendingNotifications(): List<NotificationQueue> {
        return notificationQueueRepository.findByStatus(NotificationQueue.QueueStatus.PENDING)
            .sortedBy { it.priority }
    }
    
    fun getScheduledNotifications(now: LocalDateTime = LocalDateTime.now()): List<NotificationQueue> {
        return notificationQueueRepository.findByStatusAndScheduledAtLessThanEqual(
            NotificationQueue.QueueStatus.SCHEDULED,
            now
        ).sortedBy { it.priority }
    }
    
    fun getUserNotifications(userId: UUID): List<NotificationQueue> {
        return notificationQueueRepository.findByUserId(userId)
    }
    
    fun getNotificationsByType(type: NotificationQueue.NotificationType): List<NotificationQueue> {
        return notificationQueueRepository.findByType(type)
    }
    
    fun markAsProcessing(queueId: String): NotificationQueue? {
        return notificationQueueRepository.findById(queueId).orElse(null)?.let { notification ->
            val updatedNotification = notification.markAsProcessing()
            notificationQueueRepository.save(updatedNotification)
        }
    }
    
    fun markAsCompleted(queueId: String): NotificationQueue? {
        return notificationQueueRepository.findById(queueId).orElse(null)?.let { notification ->
            val updatedNotification = notification.markAsCompleted()
            notificationQueueRepository.save(updatedNotification)
        }
    }
    
    fun markAsFailed(queueId: String, error: String): NotificationQueue? {
        return notificationQueueRepository.findById(queueId).orElse(null)?.let { notification ->
            val updatedNotification = notification.markAsFailed(error)
            notificationQueueRepository.save(updatedNotification)
        }
    }
    
    fun requeueFailedNotification(queueId: String): NotificationQueue? {
        return notificationQueueRepository.findById(queueId).orElse(null)?.let { notification ->
            if (notification.canRetry()) {
                val requeuedNotification = notification.copy(
                    status = NotificationQueue.QueueStatus.PENDING,
                    processingStartedAt = null,
                    completedAt = null
                )
                notificationQueueRepository.save(requeuedNotification)
            } else {
                null
            }
        }
    }
    
    fun deleteNotification(queueId: String) {
        notificationQueueRepository.deleteById(queueId)
    }
    
    fun cleanupOldNotifications() {
        // TTL will handle automatic cleanup, but we can manually clean if needed
        // This method can be used for custom cleanup logic
    }
}