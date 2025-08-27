package com.aiva.user.device.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "fcm_tokens")
@EntityListeners(AuditingEntityListener::class)
data class FcmToken(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "user_device_id", nullable = false)
    val userDeviceId: UUID,
    
    @Column(name = "fcm_token", nullable = false, unique = true, columnDefinition = "TEXT")
    var fcmToken: String,
    
    @Column(name = "last_validated_at")
    var lastValidatedAt: LocalDateTime? = null,
    
    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun updateToken(newToken: String) {
        this.fcmToken = newToken
        this.lastValidatedAt = LocalDateTime.now()
        this.isActive = true
    }
    
    fun revoke() {
        this.isActive = false
    }

    fun updateLastValidateAt() {
        this.lastValidatedAt = LocalDateTime.now()
    }
}
