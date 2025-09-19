package com.aiva.common.redis.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import java.time.LocalDateTime
import java.util.*

@RedisHash("auth:session")
data class AuthSession(
    @Id
    val sessionId: String,
    
    val userId: UUID,
    
    val refreshToken: String,
    
    val deviceInfo: String? = null,
    
    val ipAddress: String? = null,
    
    val userAgent: String? = null,
    
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    val lastAccessedAt: LocalDateTime = LocalDateTime.now(),
    
    @TimeToLive
    val ttl: Long = 30 * 24 * 60 * 60L // 30 days in seconds
)