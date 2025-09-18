package com.aiva.notification.domain.notification.repository

import com.aiva.notification.domain.notification.entity.NotificationRecipient
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface NotificationRecipientRepository : JpaRepository<NotificationRecipient, UUID> {
    
    fun findByNotificationIdAndUserId(notificationId: UUID, userId: UUID): NotificationRecipient?
    
    @Query("""
        SELECT nr FROM NotificationRecipient nr
        WHERE nr.userId = :userId
        AND nr.notificationId IN :notificationIds
    """)
    fun findByUserIdAndNotificationIds(
        @Param("userId") userId: UUID,
        @Param("notificationIds") notificationIds: List<UUID>
    ): List<NotificationRecipient>
}