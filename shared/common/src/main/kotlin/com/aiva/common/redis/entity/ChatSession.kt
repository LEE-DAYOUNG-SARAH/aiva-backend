package com.aiva.common.redis.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import java.time.LocalDateTime
import java.util.*

@RedisHash("chat:session")
data class ChatSession(
    @Id
    val sessionId: String,
    
    val userId: UUID,
    
    val context: List<ChatMessage> = emptyList(),
    
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    val lastActiveAt: LocalDateTime = LocalDateTime.now(),
    
    @TimeToLive
    val ttl: Long = 24 * 60 * 60L // 24 hours in seconds
) {
    data class ChatMessage(
        val role: String, // "user" or "assistant"
        val content: String,
        val timestamp: LocalDateTime = LocalDateTime.now()
    )
    
    fun addMessage(role: String, content: String): ChatSession {
        val newMessage = ChatMessage(role, content)
        val updatedContext = context.takeLast(19) + newMessage // Keep last 20 messages
        return copy(
            context = updatedContext,
            lastActiveAt = LocalDateTime.now()
        )
    }
}