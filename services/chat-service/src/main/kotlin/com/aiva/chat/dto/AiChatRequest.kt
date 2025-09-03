package com.aiva.chat.dto

import com.aiva.common.dto.ChildData
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class AiChatRequest(
    val userId: UUID,
    val isBorn: Boolean,
    val childBirthdate: LocalDate?,
    val gender: String, // FEMALE/MALE/UNKNOWN
    val question: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    
    val logId: UUID? = null
) {
    companion object {
        fun fromNewChat(userId: UUID, question: String, child: ChildData) = AiChatRequest(
            userId = userId,
            isBorn = child.isBorn,
            childBirthdate = child.childBirthdate,
            gender = child.gender,
            question = question
        )

        fun fromContinueChat(userId: UUID, question: String, child: ChildData, logId: UUID) = AiChatRequest(
            userId = userId,
            isBorn = child.isBorn,
            childBirthdate = child.childBirthdate,
            gender = child.gender,
            question = question,
            logId = logId
        )
    }
}