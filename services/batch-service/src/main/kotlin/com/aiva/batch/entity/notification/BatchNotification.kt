package com.aiva.batch.entity.notification

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

/**
 * 배치 처리용 Notification 엔티티 (읽기 전용)
 * Notification Service의 notifications 테이블과 동일한 구조
 */
@Entity
@Table(name = "notifications")
data class BatchNotification(
    @Id
    val id: UUID,
    
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    val type: NotificationType,
    
    @Column(name = "title", nullable = false, length = 200)
    val title: String,
    
    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    val body: String,
    
    @Column(name = "image_url", columnDefinition = "TEXT")
    val imageUrl: String? = null,
    
    @Column(name = "link_url", columnDefinition = "TEXT")
    val linkUrl: String? = null,
    
    @Column(name = "scheduled_at")
    val scheduledAt: LocalDateTime? = null,
    
    @Column(name = "created_by")
    val createdBy: UUID? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime,
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime
)

@Entity
@Table(name = "notification_recipients")
data class BatchNotificationRecipient(
    @Id
    val id: UUID,
    
    @Column(name = "notification_id", nullable = false)
    val notificationId: UUID,
    
    @Column(name = "user_id", nullable = false)
    val userId: UUID,
    
    @Column(name = "is_read", nullable = false)
    val isRead: Boolean = false,
    
    @Column(name = "read_at")
    val readAt: LocalDateTime? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime,
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime
)

enum class NotificationType {
    COMMUNITY_COMMENT, ANNOUNCEMENT, MARKETING, POLICY_INFO, EVENTS
}