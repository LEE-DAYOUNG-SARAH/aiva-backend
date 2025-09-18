package com.aiva.community.domain.user

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.*

/**
 * User Profile Projection for Community Service
 * 
 * User 서비스의 프로필 변경 이벤트를 받아 로컬에 저장하는 읽기 전용 프로젝션.
 * 커뮤니티 목록 조회 시 외부 서비스 호출 없이 사용자 정보를 제공한다.
 */
@Entity
@Table(
    name = "user_profile_projection",
    indexes = [
        Index(name = "idx_user_profile_updated_at", columnList = "updated_at"),
        Index(name = "idx_user_profile_nickname", columnList = "nickname"),
        Index(name = "idx_user_profile_level", columnList = "level")
    ]
)
data class UserProfileProjection(
    @Id
    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    val userId: UUID,
    
    @Column(name = "nickname", nullable = false, length = 50)
    var nickname: String = "",
    
    @Column(name = "avatar_url", columnDefinition = "TEXT")
    var avatarUrl: String? = null,
    
    @Column(name = "level", nullable = false)
    var level: Int = 0,
    
    @Column(name = "version", nullable = false)
    var version: Long = 0,
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
) {
    
    /**
     * 이벤트로부터 프로젝션을 업데이트한다.
     * 버전 체크를 통해 순서 역전을 방지한다.
     */
    fun updateFromEvent(
        nickname: String,
        avatarUrl: String?,
        level: Int,
        version: Long,
        updatedAt: Instant
    ): Boolean {
        // 순서 보장: 낮은 version이면 업데이트 하지 않음
        if (version <= this.version) {
            return false
        }
        
        this.nickname = nickname
        this.avatarUrl = avatarUrl
        this.level = level
        this.version = version
        this.updatedAt = updatedAt
        
        return true
    }
    
    /**
     * AuthorInfo DTO 변환용
     */
    fun toAuthorInfo() = AuthorInfo(
        userId = userId,
        nickname = nickname,
        profileImageUrl = avatarUrl
    )
}

/**
 * 커뮤니티에서 사용할 작성자 정보 DTO
 */
data class AuthorInfo(
    val userId: UUID,
    val nickname: String,
    val profileImageUrl: String?
)