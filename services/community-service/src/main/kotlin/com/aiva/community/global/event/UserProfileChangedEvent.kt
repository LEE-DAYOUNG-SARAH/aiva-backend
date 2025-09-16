package com.aiva.community.global.event

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.util.*

/**
 * User 서비스에서 발행하는 프로필 변경 이벤트
 * 
 * Kafka 토픽: user.profile.changed
 * Consumer Group: community
 */
data class UserProfileChangedEvent(
    @JsonProperty("userId") 
    val userId: UUID,
    
    @JsonProperty("nickname") 
    val nickname: String,
    
    @JsonProperty("avatarUrl") 
    val avatarUrl: String? = null,
    
    @JsonProperty("level") 
    val level: Int = 0,
    
    @JsonProperty("version") 
    val version: Long,
    
    @JsonProperty("updatedAt") 
    val updatedAt: Instant,
    
    @JsonProperty("eventType") 
    val eventType: UserProfileEventType = UserProfileEventType.UPDATED
) {
    
    companion object {
        const val TOPIC_NAME = "user.profile.changed"
        const val CONSUMER_GROUP = "community"
    }
}

/**
 * 사용자 프로필 이벤트 타입
 */
enum class UserProfileEventType {
    @JsonProperty("created")
    CREATED,
    
    @JsonProperty("updated") 
    UPDATED,
    
    @JsonProperty("deleted")
    DELETED
}

/**
 * 이벤트 발행을 위한 빌더
 */
class UserProfileChangedEventBuilder {
    private var userId: UUID? = null
    private var nickname: String = ""
    private var avatarUrl: String? = null
    private var level: Int = 0
    private var version: Long = 0
    private var updatedAt: Instant = Instant.now()
    private var eventType: UserProfileEventType = UserProfileEventType.UPDATED
    
    fun userId(userId: UUID) = apply { this.userId = userId }
    fun nickname(nickname: String) = apply { this.nickname = nickname }
    fun avatarUrl(avatarUrl: String?) = apply { this.avatarUrl = avatarUrl }
    fun level(level: Int) = apply { this.level = level }
    fun version(version: Long) = apply { this.version = version }
    fun updatedAt(updatedAt: Instant) = apply { this.updatedAt = updatedAt }
    fun eventType(eventType: UserProfileEventType) = apply { this.eventType = eventType }
    
    fun build(): UserProfileChangedEvent {
        requireNotNull(userId) { "userId is required" }
        require(nickname.isNotBlank()) { "nickname cannot be blank" }
        require(version > 0) { "version must be positive" }
        
        return UserProfileChangedEvent(
            userId = userId!!,
            nickname = nickname,
            avatarUrl = avatarUrl,
            level = level,
            version = version,
            updatedAt = updatedAt,
            eventType = eventType
        )
    }
}