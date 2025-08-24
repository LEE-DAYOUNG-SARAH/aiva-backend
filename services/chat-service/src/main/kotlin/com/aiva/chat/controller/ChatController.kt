package com.aiva.chat.controller

import com.aiva.security.annotation.CurrentUser
import com.aiva.security.dto.UserPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * 채팅 컨트롤러 예시
 * JWT 인증이 완료된 사용자만 접근 가능
 */
@RestController
@RequestMapping("/api/chats")
class ChatController {
    
    /**
     * 사용자의 채팅 목록 조회
     * @CurrentUser 어노테이션으로 현재 사용자 정보 자동 주입
     */
    @GetMapping
    fun getChatList(@CurrentUser currentUser: UserPrincipal): ResponseEntity<List<ChatResponse>> {
        // 현재 로그인한 사용자의 채팅 목록만 조회
        val chats = listOf(
            ChatResponse(
                id = UUID.randomUUID(),
                title = "AI 육아 상담",
                lastMessage = "안녕하세요! 어떤 고민이 있으신가요?",
                updatedAt = System.currentTimeMillis()
            )
        )
        
        return ResponseEntity.ok(chats)
    }
    
    /**
     * 새 채팅 생성
     */
    @PostMapping
    fun createChat(@CurrentUser userId: UUID): ResponseEntity<ChatResponse> {
        val newChat = ChatResponse(
            id = UUID.randomUUID(),
            title = "새로운 상담",
            lastMessage = "",
            updatedAt = System.currentTimeMillis()
        )
        
        return ResponseEntity.ok(newChat)
    }
    
    /**
     * 채팅 메시지 전송
     */
    @PostMapping("/{chatId}/messages")
    fun sendMessage(
        @PathVariable chatId: UUID,
        @CurrentUser currentUser: UserPrincipal,
        @RequestBody request: SendMessageRequest
    ): ResponseEntity<MessageResponse> {
        // AI에게 메시지 전송하고 응답 받기
        val response = MessageResponse(
            id = UUID.randomUUID(),
            chatId = chatId,
            content = "AI 응답: ${request.content}에 대한 조언을 드리겠습니다.",
            sender = "AI",
            timestamp = System.currentTimeMillis()
        )
        
        return ResponseEntity.ok(response)
    }
    
    /**
     * 사용자별 AI 사용량 조회 (구독 제한 확인용)
     */
    @GetMapping("/usage")
    fun getUsage(@CurrentUser userId: UUID): ResponseEntity<UsageResponse> {
        val usage = UsageResponse(
            userId = userId,
            monthlyUsage = 25,
            monthlyLimit = 50,
            remainingUsage = 25
        )
        
        return ResponseEntity.ok(usage)
    }
}

data class ChatResponse(
    val id: UUID,
    val title: String,
    val lastMessage: String,
    val updatedAt: Long
)

data class SendMessageRequest(
    val content: String
)

data class MessageResponse(
    val id: UUID,
    val chatId: UUID,
    val content: String,
    val sender: String,
    val timestamp: Long
)

data class UsageResponse(
    val userId: UUID,
    val monthlyUsage: Int,
    val monthlyLimit: Int,
    val remainingUsage: Int
)