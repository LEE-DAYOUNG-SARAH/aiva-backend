package com.aiva.chat.dto

import java.time.LocalDateTime
import java.util.*

data class ChatResponse(
    val id: UUID,
    val userId: UUID,
    val title: String,
    val pinned: Boolean,
    val pinnedAt: LocalDateTime?,
    val lastMessageAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)