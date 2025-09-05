package com.aiva.notification.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "notification_recipients")
@EntityListeners(AuditingEntityListener::class)
data class NotificationRecipient(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "notification_id", nullable = false)
    val notificationId: UUID,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "is_read", nullable = false)
    var isRead: Boolean = false,

    @Column(name = "read_at")
    var readAt: LocalDateTime? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @Table(
        uniqueConstraints = [
            UniqueConstraint(columnNames = ["notification_id", "user_id"])
        ]
    )
    companion object

    fun markAsRead() {
        isRead = true
        readAt = LocalDateTime.now()
    }
}