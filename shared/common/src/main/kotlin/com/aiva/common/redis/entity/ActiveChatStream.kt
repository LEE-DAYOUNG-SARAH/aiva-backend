package com.aiva.common.redis.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import java.time.LocalDateTime
import java.util.*

@RedisHash("chat:active-stream")
data class ActiveChatStream(
    @Id
    val sessionKey: String, // chatId:sessionId 형태
    
    val chatId: UUID,
    val sessionId: String,
    val userId: UUID,
    
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastActiveAt: LocalDateTime = LocalDateTime.now(),
    
    var cancelled: Boolean = false,
    
    @TimeToLive
    val ttl: Long = 2 * 60 * 60L // 2 hours in seconds
) {
    companion object {
        fun createKey(chatId: UUID, sessionId: String): String {
            return "${chatId}:${sessionId}"
        }
    }
    
    fun markActive(): ActiveChatStream {
        return copy(lastActiveAt = LocalDateTime.now())
    }
    
    fun markCancelled(): ActiveChatStream {
        return copy(cancelled = true, lastActiveAt = LocalDateTime.now())
    }
}