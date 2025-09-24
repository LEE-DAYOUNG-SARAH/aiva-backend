package com.aiva.chat.controller

import com.aiva.chat.dto.AiChatRequest
import com.aiva.chat.dto.ChatCreateResponse
import com.aiva.chat.dto.MessageRequest
import com.aiva.chat.service.ChatManagementService
import com.aiva.chat.service.ChatStreamService
import com.aiva.common.client.UserServiceClient
import com.aiva.common.dto.ChildData
import com.aiva.common.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = ["*"])
class ChatController(
    private val chatManagementService: ChatManagementService,
    private val chatStreamService: ChatStreamService,
    private val userServiceClient: UserServiceClient
) {
    
    @PostMapping("/create")
    fun createNewChat(@RequestHeader("X-User-Id") userIdString: String): ApiResponse<ChatCreateResponse> {
        val userId = UUID.fromString(userIdString)

        return ApiResponse.success(
            chatManagementService.createNewChat(userId)
        )
    }
    
    @PostMapping("/{chatId}/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamChatResponse(
        @PathVariable chatId: UUID,
        @RequestHeader("X-User-Id") userIdString: String,
        @RequestHeader("X-Session-Id") sessionId: String,
        @Valid @RequestBody request: MessageRequest
    ): Flux<String> {
        val userId = UUID.fromString(userIdString)

        val childData = userServiceClient.getChildData(userId)
            ?: throw RuntimeException("사용자 정보 조회 실패")

        // 1. 첫 메시지 여부 확인 및 사용자 메시지 저장
        val isFirstMessage = chatManagementService.isFirstMessage(chatId)
        chatManagementService.saveUserMessage(chatId, request.content)

        // 2. AI 요청 생성 및 스트리밍
        val aiRequest = createAiRequest(userId, request.content, childData, chatId, isFirstMessage)
        return chatStreamService.streamChatResponse(aiRequest, chatId, sessionId, userId, isFirstMessage)
    }
    
    @PostMapping("/{chatId}/cancel")
    fun cancelChatStream(
        @PathVariable chatId: UUID,
        @RequestHeader("X-Session-Id") sessionId: String
    ): Mono<Map<String, Any>> {
        return chatStreamService.cancelChatStream(chatId, sessionId)
            .map { cancelled ->
                mapOf(
                    "success" to cancelled,
                    "message" to if (cancelled) "채팅이 취소되었습니다" else "취소할 활성 채팅이 없습니다"
                )
            }
    }
    
    private fun createAiRequest(
        userId: UUID, 
        content: String, 
        childData: ChildData, 
        chatId: UUID, 
        isFirstMessage: Boolean
    ): AiChatRequest {
        return if (isFirstMessage) {
            AiChatRequest.fromNewChat(userId, content, childData)
        } else {
            val chat = chatManagementService.getChat(chatId)
            if (chat.logId != null) {
                AiChatRequest.fromContinueChat(userId, content, childData, UUID.fromString(chat.logId))
            } else {
                AiChatRequest.fromNewChat(userId, content, childData)
            }
        }
    }
}