package com.aiva.batch.entity.user

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

/**
 * 배치 처리용 User 엔티티 (읽기 전용)
 * User Service의 User 테이블과 동일한 구조
 */
@Entity
@Table(name = "users")
data class BatchUser(
    @Id
    val id: UUID,
    
    @Column(name = "provider", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val provider: Provider,
    
    @Column(name = "provider_user_id", nullable = false, length = 128)
    val providerUserId: String,
    
    @Column(name = "email", length = 255)
    val email: String? = null,
    
    @Column(name = "nickname", nullable = false, length = 10)
    val nickname: String,
    
    @Column(name = "avatar_url", columnDefinition = "TEXT")
    val avatarUrl: String? = null,
    
    @Column(name = "is_pro", nullable = false)
    val isPro: Boolean = false,
    
    @Column(name = "pro_expires_at")
    val proExpiresAt: LocalDateTime? = null,
    
    @Column(name = "last_login_at")
    val lastLoginAt: LocalDateTime? = null,
    
    @Column(name = "deleted_at")
    val deletedAt: LocalDateTime? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime,
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime
)

enum class Provider {
    KAKAO, GOOGLE
}