package com.aiva.chat.dto

import java.util.*

sealed class ChatStreamEvent {
    data class MessageChunk(
        val chatId: UUID,
        val content: String,
        val isComplete: Boolean = false
    ) : ChatStreamEvent()
    
    data class MessageComplete(
        val chatId: UUID,
        val messageId: UUID,
        val content: String
    ) : ChatStreamEvent()
    
    data class Error(
        val message: String,
        val code: String? = null
    ) : ChatStreamEvent()
}