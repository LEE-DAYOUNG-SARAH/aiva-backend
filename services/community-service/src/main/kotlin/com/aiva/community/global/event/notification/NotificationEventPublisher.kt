package com.aiva.community.global.event.notification

import com.aiva.community.global.event.publisher.EventPublisher
import com.aiva.community.global.event.topic.KafkaTopics
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * 알림 이벤트 발행 서비스 (리팩토링)
 * 
 * 클린코드 원칙 적용:
 * - 단일 책임 원칙: 알림 이벤트 발행만 담당
 * - 의존성 역전 원칙: EventPublisher 인터페이스에 의존
 * - 개방-폐쇄 원칙: 새로운 발행 방식 추가 시 확장 가능
 */
@Service
class NotificationEventPublisher(
    private val eventPublisher: EventPublisher
) {
    
    private val logger = KotlinLogging.logger {}
    
    /**
     * 알림 이벤트를 비동기로 발행
     * 
     * @param event 발행할 알림 이벤트
     */
    fun publishAsync(event: NotificationEvent) {
        try {
            val key = generatePartitionKey(event)
            eventPublisher.publishAsync(KafkaTopics.COMMUNITY_NOTIFICATION, key, event)
            
            logger.debug { "Initiated async publishing for notification event: ${event.eventId}" }
            
        } catch (e: Exception) {
            logger.error(e) { "Exception occurred while publishing notification event: ${event.eventId}" }
            throw NotificationPublishException("Failed to publish notification event", event, e)
        }
    }
    
    /**
     * 트랜잭션 커밋 후 이벤트 발행
     * Spring의 @TransactionalEventListener를 통해 트랜잭션 안전성 보장
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleNotificationApplicationEventAfterCommit(applicationEvent: NotificationApplicationEvent) {
        val event = applicationEvent.notificationEvent
        logger.debug { "Publishing notification event after transaction commit: ${event.eventId}" }
        publishAsync(event)
    }
    
    /**
     * Kafka 파티션 키 생성
     * 같은 사용자의 알림은 같은 파티션으로 보내서 순서 보장
     */
    private fun generatePartitionKey(event: NotificationEvent): String {
        return "${event.targetUserId}-${event.eventType}"
    }
    
    /**
     * 알림 발행 전용 예외 클래스
     */
    class NotificationPublishException(
        message: String,
        val event: NotificationEvent,
        cause: Throwable
    ) : RuntimeException(message, cause)
}

/**
 * 알림 이벤트 발행을 위한 Application Event
 * Spring의 이벤트 시스템을 통해 비동기 처리
 */
data class NotificationApplicationEvent(
    val notificationEvent: NotificationEvent
)