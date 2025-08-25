package com.aiva.user.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
class User(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "provider", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val provider: Provider,
    
    @Column(name = "provider_user_id", nullable = false, length = 128)
    val providerUserId: String,
    
    @Column(name = "email", length = 255)
    val email: String? = null,
    
    @Column(name = "nickname", nullable = false, length = 10)
    var nickname: String,
    
    @Column(name = "avatar_url", columnDefinition = "TEXT")
    var avatarUrl: String? = null,
    
    @Column(name = "is_pro", nullable = false)
    var isPro: Boolean = false,
    
    @Column(name = "pro_expires_at")
    var proExpiresAt: LocalDateTime? = null,
    
    @Column(name = "last_login_at")
    var lastLoginAt: LocalDateTime? = null,
    
    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null,
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now()
    }
}

enum class Provider {
    KAKAO, GOOGLE
}
