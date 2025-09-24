package com.aiva.common.redis.service

import com.aiva.common.redis.entity.ActiveChatStream
import com.aiva.common.redis.repository.ActiveChatStreamRepository
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Service
class ActiveChatStreamService(
    private val activeChatStreamRepository: ActiveChatStreamRepository,
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>
) {
    
    companion object {
        private const val STREAM_CANCEL_CHANNEL = "chat:stream:cancel"
        private const val STREAM_STATUS_CHANNEL = "chat:stream:status"
    }
    
    fun createActiveStream(chatId: UUID, sessionId: String, userId: UUID): ActiveChatStream {
        val sessionKey = ActiveChatStream.createKey(chatId, sessionId)
        val activeStream = ActiveChatStream(
            sessionKey = sessionKey,
            chatId = chatId,
            sessionId = sessionId,
            userId = userId
        )
        return activeChatStreamRepository.save(activeStream)
    }
    
    fun markStreamActive(chatId: UUID, sessionId: String): Boolean {
        val sessionKey = ActiveChatStream.createKey(chatId, sessionId)
        return activeChatStreamRepository.findById(sessionKey)
            .map { stream ->
                activeChatStreamRepository.save(stream.markActive())
                true
            }
            .orElse(false)
    }
    
    fun cancelStream(chatId: UUID, sessionId: String): Mono<Boolean> {
        val sessionKey = ActiveChatStream.createKey(chatId, sessionId)
        
        return Mono.fromCallable {
            activeChatStreamRepository.findById(sessionKey)
        }
        .flatMap { optionalStream ->
            if (optionalStream.isPresent) {
                val stream = optionalStream.get()
                val cancelledStream = stream.markCancelled()
                activeChatStreamRepository.save(cancelledStream)
                
                // Redis Pub/Sub로 취소 신호 전송
                reactiveRedisTemplate.convertAndSend(STREAM_CANCEL_CHANNEL, sessionKey)
                    .map { true }
            } else {
                Mono.just(false)
            }
        }
        .onErrorReturn(false)
    }
    
    fun listenForCancellation(chatId: UUID, sessionId: String): Flux<String> {
        val sessionKey = ActiveChatStream.createKey(chatId, sessionId)
        
        return reactiveRedisTemplate
            .listenToChannel(STREAM_CANCEL_CHANNEL)
            .filter { message -> message.message == sessionKey }
            .map { it.message }
    }
    
    fun isStreamActive(chatId: UUID, sessionId: String): Boolean {
        val sessionKey = ActiveChatStream.createKey(chatId, sessionId)
        return activeChatStreamRepository.findById(sessionKey)
            .map { stream -> !stream.cancelled }
            .orElse(false)
    }
    
    fun getActiveStreamsByChatId(chatId: UUID): List<ActiveChatStream> {
        return activeChatStreamRepository.findByChatId(chatId)
            .filter { !it.cancelled }
    }
    
    fun cleanupFinishedStream(chatId: UUID, sessionId: String) {
        val sessionKey = ActiveChatStream.createKey(chatId, sessionId)
        activeChatStreamRepository.deleteById(sessionKey)
    }
    
    fun cleanupUserStreams(userId: UUID) {
        activeChatStreamRepository.deleteByUserId(userId)
    }
}