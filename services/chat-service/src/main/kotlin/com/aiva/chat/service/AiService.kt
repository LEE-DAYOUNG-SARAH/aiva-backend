package com.aiva.chat.service

import com.aiva.chat.dto.AiChatRequest
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import java.util.concurrent.ConcurrentHashMap

@Service
class AiService(
    @Qualifier("aiWebClient") private val webClient: WebClient
) {
    
    private val activeConnections = ConcurrentHashMap<AiChatRequest, () -> Unit>()
    
    fun streamChatResponse(request: AiChatRequest): Flux<String> {
        return webClient
            .post()
            .uri("/api/v1/chat")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .bodyValue(request)
            .retrieve()
            .bodyToFlux(String::class.java)
            .doOnSubscribe {
                // 연결 추적 시작
                activeConnections[request] = {
                    // 실제 연결 취소 로직은 WebClient가 자동 처리
                }
            }
            .doFinally {
                // 연결 정리
                activeConnections.remove(request)
            }
            .onErrorResume { error ->
                Flux.error(RuntimeException("AI 서버 호출 실패: ${error.message}", error))
            }
    }
    
    fun cancelChatStream(request: AiChatRequest) {
        activeConnections.remove(request)?.invoke()
    }
}