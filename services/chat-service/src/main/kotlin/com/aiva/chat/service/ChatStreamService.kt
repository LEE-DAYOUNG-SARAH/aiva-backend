package com.aiva.chat.service

import com.aiva.chat.dto.AiChatRequest
import com.aiva.common.redis.service.ActiveChatStreamService
import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Service
class ChatStreamService(
    private val chatManagementService: ChatManagementService,
    private val aiService: AiService,
    private val aiResponseParser: AiResponseParser,
    private val activeChatStreamService: ActiveChatStreamService
) {
    
    private val logger = KotlinLogging.logger {}
    
    fun streamChatResponse(request: AiChatRequest, chatId: UUID, sessionId: String, userId: UUID, isFirstChat: Boolean = false): Flux<String> {
        val state = ChatStreamingState(chatId, isFirstChat)
        
        // Redis에 활성 스트림 등록
        activeChatStreamService.createActiveStream(chatId, sessionId, userId)
        logger.info { "스트림 세션 시작: chatId=$chatId, sessionId=$sessionId" }
        
        // Redis Pub/Sub 취소 신호 구독
        val cancellationSignal = activeChatStreamService.listenForCancellation(chatId, sessionId)
            .next() // 첫 번째 취소 신호만 처리
            .map { true }
        
        return aiService.streamChatResponse(request)
            .doOnNext { line -> 
                processStreamLine(line, state)
                activeChatStreamService.markStreamActive(chatId, sessionId) // 활성 상태 갱신
            }
            .takeUntilOther(cancellationSignal)
            .doOnCancel { handleCancellation(request, state) }
            .doFinally { finalizeStream(chatId, sessionId, state) }
    }
    
    
    private fun processStreamLine(line: String, state: ChatStreamingState) {
        val event = aiResponseParser.parseEventLine(line) ?: return
        
        when (event) {
            is AiEvent.Data -> processDataEvent(event.content, state)
            else -> {} // 다른 이벤트는 현재 무시
        }
    }
    
    private fun processDataEvent(dataNode: JsonNode, state: ChatStreamingState) {
        // 로그 ID 추출 (첫 채팅만)
        aiResponseParser.extractLogId(dataNode)?.let { logId ->
            state.setLogId(logId, state.isFirstChat)
        }
        
        // 델타 텍스트 추출
        aiResponseParser.extractDelta(dataNode)?.let { delta ->
            state.appendText(delta)
        }
        
        // 메타데이터 처리 (출처만)
        if (dataNode.has("sources")) {
            processMetadataEvent(dataNode, state)
        }
    }
    
    private fun processMetadataEvent(dataNode: com.fasterxml.jackson.databind.JsonNode, state: ChatStreamingState) {
        // 출처 정보 추출만
        val sources = aiResponseParser.extractSources(dataNode)
        if (sources.isNotEmpty()) {
            state.addSources(sources)
        }
    }
    
    private fun handleCancellation(request: AiChatRequest, state: ChatStreamingState) {
        state.markCancelled()
        aiService.cancelChatStream(request)
    }
    
    private fun finalizeStream(chatId: UUID, sessionId: String, state: ChatStreamingState) {
        logger.info { "스트림 세션 종료: chatId=$chatId, sessionId=$sessionId" }
        
        // Redis에서 활성 스트림 정리
        activeChatStreamService.cleanupFinishedStream(chatId, sessionId)
        
        if (state.hasContent()) {
            saveStreamResult(state)
        }
    }
    
    private fun saveStreamResult(state: ChatStreamingState) {
        if (state.isFirstChat && state.logId != null) {
            chatManagementService.saveFirstChatResponse(
                chatId = state.chatId,
                content = state.textContent,
                logId = state.logId!!,
                sources = state.sources,
                stoppedByUser = state.userCancelled
            )
        } else {
            chatManagementService.saveAiMessageWithSources(
                chatId = state.chatId,
                content = state.textContent,
                sources = state.sources,
                stoppedByUser = state.userCancelled
            )
        }
    }
    
    fun cancelChatStream(chatId: UUID, sessionId: String): Mono<Boolean> {
        logger.info { "스트림 취소 요청: chatId=$chatId, sessionId=$sessionId" }
        return activeChatStreamService.cancelStream(chatId, sessionId)
    }
}