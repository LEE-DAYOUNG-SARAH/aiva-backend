package com.aiva.common.redis.repository

import com.aiva.common.redis.entity.NotificationQueue
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface NotificationQueueRepository : CrudRepository<NotificationQueue, String> {
    
    fun findByUserId(userId: UUID): List<NotificationQueue>
    
    fun findByStatus(status: NotificationQueue.QueueStatus): List<NotificationQueue>
    
    fun findByType(type: NotificationQueue.NotificationType): List<NotificationQueue>
    
    fun findByStatusAndScheduledAtLessThanEqual(
        status: NotificationQueue.QueueStatus,
        scheduledAt: java.time.LocalDateTime
    ): List<NotificationQueue>
}