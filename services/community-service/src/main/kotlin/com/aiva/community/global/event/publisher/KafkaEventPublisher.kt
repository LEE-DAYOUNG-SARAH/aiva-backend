package com.aiva.community.global.event.publisher

import mu.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

/**
 * Kafka 기반 이벤트 발행 구현체
 * 
 * EventPublisher 인터페이스의 Kafka 구현체로,
 * 실제 Kafka 브로커와의 통신을 담당합니다.
 */
@Service
class KafkaEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) : EventPublisher {
    
    private val logger = KotlinLogging.logger {}
    
    override fun publishAsync(topic: String, key: String, event: Any) {
        try {
            val future: CompletableFuture<SendResult<String, Any>> = kafkaTemplate.send(topic, key, event)
            
            future.whenComplete { result, throwable ->
                if (throwable != null) {
                    handlePublishFailure(topic, key, event, throwable)
                } else {
                    handlePublishSuccess(topic, key, event, result)
                }
            }
            
        } catch (e: Exception) {
            logger.error(e) { "Exception occurred while publishing event to topic: $topic, key: $key" }
            handlePublishFailure(topic, key, event, e)
        }
    }
    
    override fun publishSync(topic: String, key: String, event: Any) {
        try {
            val result = kafkaTemplate.send(topic, key, event).get()
            handlePublishSuccess(topic, key, event, result)
        } catch (e: Exception) {
            logger.error(e) { "Failed to publish event synchronously to topic: $topic, key: $key" }
            handlePublishFailure(topic, key, event, e)
            throw e
        }
    }
    
    private fun handlePublishSuccess(
        topic: String, 
        key: String, 
        event: Any, 
        result: SendResult<String, Any>
    ) {
        val metadata = result.recordMetadata
        logger.debug { 
            "Successfully published event to topic: $topic, key: $key, " +
            "partition: ${metadata.partition()}, offset: ${metadata.offset()}" 
        }
    }
    
    private fun handlePublishFailure(
        topic: String, 
        key: String, 
        event: Any, 
        throwable: Throwable
    ) {
        logger.error(throwable) { 
            "Failed to publish event to topic: $topic, key: $key, event: $event" 
        }
        
        // TODO: 실패한 이벤트를 Dead Letter Queue나 별도 저장소에 보관
        // TODO: 재시도 로직 구현 (exponential backoff)
        // TODO: 모니터링 메트릭 전송
    }
}