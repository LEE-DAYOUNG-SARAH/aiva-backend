package com.aiva.common.kafka.handler

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import mu.KotlinLogging
import org.springframework.kafka.listener.ConsumerAwareListenerErrorHandler
import org.springframework.kafka.listener.ListenerExecutionFailedException
import org.springframework.stereotype.Component

/**
 * 공통 Kafka 에러 핸들러
 * 
 * 모든 Kafka 리스너에서 발생하는 예외를 일관성 있게 처리합니다.
 */
@Component("kafkaErrorHandler")
class KafkaErrorHandler : ConsumerAwareListenerErrorHandler {
    
    private val logger = KotlinLogging.logger {}
    
    override fun handleError(
        message: org.springframework.messaging.Message<*>,
        exception: ListenerExecutionFailedException,
        consumer: Consumer<*, *>
    ): Any {
        val record = extractConsumerRecord(message)
        val topic = record?.topic() ?: "unknown"
        val partition = record?.partition() ?: -1
        val offset = record?.offset() ?: -1
        val value = record?.value()?.toString() ?: "unknown"
        
        logger.error(exception) {
            "Kafka message processing failed. Topic: $topic, Partition: $partition, Offset: $offset, Value: $value"
        }
        
        // 특정 예외 타입에 따른 처리
        when (exception.cause) {
            is IllegalArgumentException -> {
                logger.warn { "Skipping invalid message due to illegal argument: $value" }
                return message // 메시지 스킵
            }
            is org.springframework.dao.DataIntegrityViolationException -> {
                logger.warn { "Data integrity violation, skipping message: $value" }
                return message // 메시지 스킵
            }
            else -> {
                // 재시도 가능한 예외는 다시 throw하여 재시도하도록 함
                throw exception
            }
        }
    }
    
    private fun extractConsumerRecord(message: org.springframework.messaging.Message<*>): ConsumerRecord<*, *>? {
        return try {
            message.headers["kafka_receivedMessageKey"] as? ConsumerRecord<*, *>
        } catch (e: Exception) {
            logger.debug(e) { "Could not extract ConsumerRecord from message" }
            null
        }
    }
}