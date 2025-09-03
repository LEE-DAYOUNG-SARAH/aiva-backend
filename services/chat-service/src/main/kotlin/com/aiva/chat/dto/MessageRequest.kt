package com.aiva.chat.dto

import jakarta.validation.constraints.NotBlank

data class MessageRequest(
    @field:NotBlank(message = "메시지 내용은 필수입니다")
    val content: String
)