package com.aiva.chat.service

import com.aiva.chat.entity.MessageSource
import java.util.*

data class ChatStreamingState(
    val chatId: UUID,
    val isFirstChat: Boolean,
    var textContent: String = "",
    var logId: String? = null,
    val sources: MutableList<MessageSource> = mutableListOf(),
    var userCancelled: Boolean = false
) {
    
    fun appendText(delta: String) {
        textContent += delta
    }
    
    fun setLogId(logId: String, shouldSave: Boolean) {
        if (shouldSave) {
            this.logId = logId
        }
    }
    
    fun addSources(newSources: List<MessageSource>) {
        sources.addAll(newSources.map { source ->
            source.copy(messageId = UUID.randomUUID()) // 임시 ID
        })
    }
    
    fun markCancelled() {
        userCancelled = true
    }
    
    fun hasContent(): Boolean = textContent.isNotEmpty()
}