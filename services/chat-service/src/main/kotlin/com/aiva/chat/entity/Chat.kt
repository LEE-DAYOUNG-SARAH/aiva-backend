package com.aiva.chat.entity

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "chats")
data class Chat(
    @Id
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)")
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "user_id", columnDefinition = "BINARY(16)", nullable = false)
    val userId: UUID,
    
    @Column(name = "title", nullable = false, length = 200)
    var title: String? = null,
    
    @Column(name = "pinned", nullable = false)
    var pinned: Boolean = false,
    
    @Column(name = "pinned_at")
    var pinnedAt: LocalDateTime? = null,
    
    @Column(name = "last_message_at")
    var lastMessageAt: LocalDateTime? = null,
    
    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null,

    @Column(name = "summary", length = 200)
    val summary: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "log_id")
    var logId: String? = null,

    @OneToMany(mappedBy = "chatId", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val messages: List<Message> = emptyList()
)