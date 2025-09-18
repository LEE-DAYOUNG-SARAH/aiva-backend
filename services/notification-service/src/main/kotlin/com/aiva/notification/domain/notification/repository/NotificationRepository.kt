package com.aiva.notification.repository

import com.aiva.notification.entity.Notification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Repository
interface NotificationRepository : JpaRepository<Notification, UUID> {
    
    fun findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
        userId: UUID,
        startDate: LocalDateTime,
        pageable: Pageable
    ): Page<Notification>
    
    fun countByUserIdAndCreatedAtAfter(
        userId: UUID,
        startDate: LocalDateTime
    ): Long
    
    
    @Modifying
    @Transactional
    @Query("""
        UPDATE Notification n
        SET n.isRead = true, n.readAt = :readAt
        WHERE n.userId = :userId AND n.isRead = false
        AND (:beforeDate IS NULL OR n.createdAt <= :beforeDate)
    """)
    fun markAllAsRead(
        @Param("userId") userId: UUID,
        @Param("readAt") readAt: LocalDateTime,
        @Param("beforeDate") beforeDate: LocalDateTime?
    ): Int
}