package com.aiva.user.notification.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "user_notification_permissions",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "permission_key"])
    ]
)
@EntityListeners(AuditingEntityListener::class)
data class UserNotificationSetting(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "user_id", nullable = false)
    val userId: UUID,
    
    @Column(name = "permission_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    val permissionType: NotificationPermissionType,
    
    @Column(name = "is_enabled", nullable = false)
    var isEnabled: Boolean = true,
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun updateEnabled(isEnabled: Boolean) {
        this.isEnabled = isEnabled
    }
}

/**
 * 알림 권한 타입 enum
 */
enum class NotificationPermissionType(val key: String, val displayName: String, val defaultValue: Boolean) {
    POLICY_INFO("policy_info", "정책/정보 알림", true),
    COMMUNITY("community", "커뮤니티 알림", true),
    BILLING("billing", "구독/결제 알림", true),
    MARKETING("marketing", "마케팅 알림", false)
}
