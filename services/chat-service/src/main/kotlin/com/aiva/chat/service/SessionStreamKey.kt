package com.aiva.chat.service

import java.util.*

data class SessionStreamKey(
    val chatId: UUID,
    val sessionId: String
) {
    override fun toString(): String = "Chat:$chatId-Session:$sessionId"
}