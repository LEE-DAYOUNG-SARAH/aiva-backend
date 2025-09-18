package com.aiva.chat.dto

import com.aiva.chat.entity.MessageRole
import java.time.LocalDateTime
import java.util.*

data class MessageResponse(
    val id: UUID,
    val chatId: UUID,
    val role: MessageRole,
    val content: String,
    val stoppedByUser: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)