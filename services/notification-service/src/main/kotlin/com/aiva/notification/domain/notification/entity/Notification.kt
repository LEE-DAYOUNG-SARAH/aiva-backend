package com.aiva.notification.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "notifications")
@EntityListeners(AuditingEntityListener::class)
data class Notification(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "type", nullable = false, length = 20)
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
    
    @Column(name = "user_id", nullable = false)
    val userId: UUID,
    
    @Column(name = "is_read", nullable = false)
    var isRead: Boolean = false,
    
    @Column(name = "read_at")
    var readAt: LocalDateTime? = null,
    
    @Column(name = "scheduled_at")
    val scheduledAt: LocalDateTime? = null,
    
    @Column(name = "created_by")
    val createdBy: UUID? = null,
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @OneToMany(mappedBy = "notificationId", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val recipients: List<NotificationRecipient> = mutableListOf()
) {
    fun markAsRead() {
        isRead = true
        readAt = LocalDateTime.now()
    }
}

enum class NotificationType {
    COMMUNITY_COMMENT,
    ANNOUNCEMENT,
    MARKETING,
    POLICY_INFO,
    EVENTS
}
