package com.aiva.common.redis.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import org.springframework.data.redis.core.index.Indexed
import java.time.LocalDateTime
import java.util.*

@RedisHash("notification:queue")
data class NotificationQueue(
    @Id
    val queueId: String,
    
    val notificationId: UUID,
    
    val userId: UUID,
    
    @Indexed
    val type: NotificationType,
    
    val title: String,
    
    val body: String,
    
    val imageUrl: String? = null,
    
    val linkUrl: String? = null,
    
    val payload: Map<String, Any> = emptyMap(),
    
    @Indexed
    val status: QueueStatus = QueueStatus.PENDING,
    
    val priority: Int = 5, // 1 (highest) to 10 (lowest)
    
    val scheduledAt: LocalDateTime? = null,
    
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    val processingStartedAt: LocalDateTime? = null,
    
    val completedAt: LocalDateTime? = null,
    
    val retryCount: Int = 0,
    
    val maxRetryCount: Int = 3,
    
    val lastError: String? = null,
    
    @TimeToLive
    val ttl: Long = 7 * 24 * 60 * 60L // 7 days in seconds
) {
    enum class NotificationType {
        COMMUNITY_COMMENT,
        COMMUNITY_LIKE,
        SUBSCRIPTION_EXPIRING,
        SUBSCRIPTION_RENEWED,
        SYSTEM_ANNOUNCEMENT,
        MARKETING,
        EVENTS
    }
    
    enum class QueueStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        SCHEDULED
    }
    
    fun markAsProcessing(): NotificationQueue = copy(
        status = QueueStatus.PROCESSING,
        processingStartedAt = LocalDateTime.now()
    )
    
    fun markAsCompleted(): NotificationQueue = copy(
        status = QueueStatus.COMPLETED,
        completedAt = LocalDateTime.now()
    )
    
    fun markAsFailed(error: String): NotificationQueue = copy(
        status = QueueStatus.FAILED,
        retryCount = retryCount + 1,
        lastError = error
    )
    
    fun canRetry(): Boolean = retryCount < maxRetryCount
}