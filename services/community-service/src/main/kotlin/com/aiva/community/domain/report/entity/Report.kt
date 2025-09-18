package com.aiva.community.domain.report.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "reports")
@EntityListeners(AuditingEntityListener::class)
data class Report(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "reporter_user_id", nullable = false)
    val reporterUserId: UUID,

    @Column(name = "target_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val targetType: ReportTargetType,

    @Column(name = "target_id", nullable = false)
    val targetId: UUID,

    @Column(name = "reason_code", nullable = false, length = 30)
    val reasonCode: String,

    @Column(name = "details", columnDefinition = "TEXT")
    val details: String? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class ReportTargetType {
    POST, COMMENT
}