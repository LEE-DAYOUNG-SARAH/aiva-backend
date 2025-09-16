package com.aiva.chat.service

import com.aiva.chat.dto.AiChatRequest
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class ChatStreamService(
    private val chatManagementService: ChatManagementService,
    private val aiService: AiService,
    private val aiResponseParser: AiResponseParser
) {
    
    private val activeSessions = ConcurrentHashMap<SessionStreamKey, Sinks.One<Boolean>>()
    
    fun streamChatResponse(request: AiChatRequest, chatId: UUID, sessionId: String, isFirstChat: Boolean = false): Flux<String> {
        val state = ChatStreamingState(chatId, isFirstChat)
        val sessionKey = SessionStreamKey(chatId, sessionId)
        val cancellationSink = createCancellationSink(sessionKey)
        
        return aiService.streamChatResponse(request)
            .doOnNext { line -> processStreamLine(line, state) }
            .takeUntilOther(cancellationSink.asMono())
            .doOnCancel { handleCancellation(request, state) }
            .doFinally { finalizeStream(sessionKey, state) }
    }
    
    private fun createCancellationSink(sessionKey: SessionStreamKey): Sinks.One<Boolean> {
        val cancellationSink = Sinks.one<Boolean>()
        activeSessions[sessionKey] = cancellationSink
        return cancellationSink
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
    
    private fun finalizeStream(sessionKey: SessionStreamKey, state: ChatStreamingState) {
        activeSessions.remove(sessionKey)
        
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
    
    fun cancelChatStream(chatId: UUID, sessionId: String): Boolean {
        val sessionKey = SessionStreamKey(chatId, sessionId)
        return activeSessions[sessionKey]?.let { sink ->
            sink.tryEmitValue(true)
            true
        } ?: false
    }
}