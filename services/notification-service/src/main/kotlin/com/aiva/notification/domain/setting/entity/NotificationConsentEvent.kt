package com.aiva.notification.domain.setting.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "notification_consent_events",
    indexes = [
        Index(name = "ix_nce_user_cat_created", columnList = "user_id, permission_type, created_at DESC"),
        Index(name = "ix_nce_user_created", columnList = "user_id, created_at DESC")
    ]
)
@EntityListeners(AuditingEntityListener::class)
data class NotificationConsentEvent(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "permission_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    val permissionType: NotificationPermissionType,

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 10)
    val action: ConsentAction,

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 30)
    val source: ConsentSource,

    @Column(name = "policy_version", nullable = false, length = 50)
    val policyVersion: String,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class ConsentAction {
    OPT_IN, OPT_OUT
}

enum class ConsentSource {
    SYSTEM, MYPAGE_TOGGLE, ADMIN_CONSOLE, MIGRATION
}