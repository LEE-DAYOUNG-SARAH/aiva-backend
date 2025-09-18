package com.aiva.community.global.event.notification

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * 알림 이벤트 발행 서비스
 * 
 * 커뮤니티 서비스에서 발생하는 알림 이벤트를 Kafka로 발행합니다.
 * 트랜잭션 커밋 후에 이벤트를 발행하여 데이터 일관성을 보장합니다.
 */
@Service
class NotificationEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val objectMapper: ObjectMapper
) {
    
    private val logger = LoggerFactory.getLogger(NotificationEventPublisher::class.java)
    
    /**
     * 알림 이벤트를 비동기로 발행
     * 
     * @param event 발행할 알림 이벤트
     */
    fun publishAsync(event: NotificationEvent) {
        try {
            val key = generateKey(event)
            val future: CompletableFuture<SendResult<String, Any>> = kafkaTemplate.send(
                NotificationEvent.TOPIC_NAME,
                key,
                event
            )
            
            future.whenComplete { result, throwable ->
                if (throwable != null) {
                    logger.error("Failed to publish notification event: ${event.eventId}", throwable)
                    handlePublishFailure(event, throwable)
                } else {
                    logger.debug("Successfully published notification event: ${event.eventId} to partition: ${result.recordMetadata.partition()}")
                }
            }
            
        } catch (e: Exception) {
            logger.error("Exception occurred while publishing notification event: ${event.eventId}", e)
            handlePublishFailure(event, e)
        }
    }
    
    /**
     * 트랜잭션 커밋 후 이벤트 발행
     * Spring의 @TransactionalEventListener를 통해 트랜잭션 안전성 보장
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleNotificationApplicationEventAfterCommit(applicationEvent: NotificationApplicationEvent) {
        val event = applicationEvent.notificationEvent
        logger.debug("Publishing notification event after transaction commit: ${event.eventId}")
        publishAsync(event)
    }
    
    /**
     * Kafka 파티션 키 생성
     * 같은 사용자의 알림은 같은 파티션으로 보내서 순서 보장
     */
    private fun generateKey(event: NotificationEvent): String {
        return "${event.targetUserId}-${event.eventType}"
    }
    
    /**
     * 발행 실패 처리
     * 실패한 이벤트를 로그에 기록하고, 필요시 재시도 로직 추가 가능
     */
    private fun handlePublishFailure(event: NotificationEvent, throwable: Throwable) {
        logger.error("Failed to publish notification event: ${objectMapper.writeValueAsString(event)}", throwable)
        
        // TODO: 실패한 이벤트를 Dead Letter Queue나 별도 저장소에 보관
        // TODO: 재시도 로직 구현 (exponential backoff)
        // TODO: 모니터링 메트릭 전송
    }
}

/**
 * 알림 이벤트 발행을 위한 Application Event
 * Spring의 이벤트 시스템을 통해 비동기 처리
 */
data class NotificationApplicationEvent(
    val notificationEvent: NotificationEvent
)