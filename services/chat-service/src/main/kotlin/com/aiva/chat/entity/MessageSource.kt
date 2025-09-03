package com.aiva.chat.entity

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "message_sources")
data class MessageSource(
    @Id
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)")
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "message_id", columnDefinition = "BINARY(16)", nullable = false)
    val messageId: UUID,
    
    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "link", nullable = true)
    val link: String?,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)