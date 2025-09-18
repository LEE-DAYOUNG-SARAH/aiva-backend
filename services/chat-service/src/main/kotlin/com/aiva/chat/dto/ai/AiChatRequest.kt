package com.aiva.chat.dto.ai

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.util.*

data class AiChatRequest(
    @JsonProperty("userId")
    val userId: UUID,
    
    @JsonProperty("isBorn")
    val isBorn: Boolean,
    
    @JsonProperty("childBirthdate")
    val childBirthdate: String? = null,
    
    @JsonProperty("gender")
    val gender: String, // FEMALE/MALE/UNKNOWN
    
    @JsonProperty("question")
    val question: String,
    
    @JsonProperty("timestamp")
    val timestamp: LocalDateTime,
    
    @JsonProperty("logId")
    val logId: UUID? = null
)