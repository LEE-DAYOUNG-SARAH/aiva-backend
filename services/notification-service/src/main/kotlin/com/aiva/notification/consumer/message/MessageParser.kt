package com.aiva.notification.consumer.message

import com.aiva.notification.domain.notification.dto.CommunityNotificationEvent
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.stereotype.Component

/**
 * Kafka 메시지 파싱 담당 클래스
 * 
 * 단일 책임: JSON 메시지를 도메인 객체로 변환
 */
@Component
class MessageParser(
    private val objectMapper: ObjectMapper
) {
    
    private val logger = KotlinLogging.logger {}
    
    /**
     * JSON 메시지를 CommunityNotificationEvent로 파싱
     * 
     * @param message JSON 형태의 메시지
     * @return 파싱된 이벤트 객체
     * @throws MessageParsingException 파싱 실패 시
     */
    fun parseCommunityNotificationEvent(message: String): CommunityNotificationEvent {
        return try {
            logger.debug { "Parsing community notification message: $message" }
            objectMapper.readValue(message, CommunityNotificationEvent::class.java)
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse community notification message: $message" }
            throw MessageParsingException("Invalid message format", message, e)
        }
    }
    
    /**
     * 메시지 파싱 전용 예외 클래스
     */
    class MessageParsingException(
        message: String,
        val originalMessage: String,
        cause: Throwable
    ) : RuntimeException(message, cause)
}