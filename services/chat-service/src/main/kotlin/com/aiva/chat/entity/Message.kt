package com.aiva.chat.entity

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "messages")
data class Message(
    @Id
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)")
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "chat_id", columnDefinition = "BINARY(16)", nullable = false)
    val chatId: UUID,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    val role: MessageRole,
    
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    val content: String,
    
    @Column(name = "stopped_by_user")
    val stoppedByUser: Boolean = false,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @OneToMany(mappedBy = "messageId", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val sources: List<MessageSource> = emptyList()
)

enum class MessageRole {
    USER,
    ASSISTANT
}