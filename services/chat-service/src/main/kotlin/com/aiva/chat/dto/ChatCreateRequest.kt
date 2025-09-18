package com.aiva.chat.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.*

data class ChatCreateRequest(
    @field:NotNull(message = "사용자 ID는 필수입니다")
    val userId: UUID,
    
    @field:NotBlank(message = "메시지 내용은 필수입니다")
    val message: String
)