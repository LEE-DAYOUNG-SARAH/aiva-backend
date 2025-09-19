package com.aiva.notification.consumer

import com.aiva.notification.consumer.delivery.NotificationDeliveryService
import com.aiva.notification.consumer.message.MessageParser
import com.aiva.notification.consumer.persistence.NotificationPersistenceService
import mu.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

/**
 * 리팩토링된 알림 컨슈머
 * 
 * 단일 책임: Kafka 메시지 수신 및 전체 알림 처리 흐름 조율
 * 
 * 클린코드 원칙 적용:
 * - 단일 책임 원칙: 각 단계를 별도 서비스로 분리
 * - 의존성 역전 원칙: 인터페이스 기반 의존성 주입
 * - 개방-폐쇄 원칙: 새로운 알림 타입 추가 시 확장 가능
 */
@Component
class NotificationConsumerRefactored(
    private val messageParser: MessageParser,
    private val persistenceService: NotificationPersistenceService,
    private val deliveryService: NotificationDeliveryService
) {
    
    private val logger = KotlinLogging.logger {}
    
    @KafkaListener(
        topics = ["\${app.kafka.topics.community-notification.name:community.notification}"], 
        groupId = "\${app.kafka.topics.community-notification.consumer-group:notification-service-group}",
        containerFactory = "kafkaListenerContainerFactory",
        errorHandler = "kafkaErrorHandler"
    )
    fun handleCommunityNotification(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment?
    ) {
        logger.info { "Processing community notification from topic: $topic, partition: $partition, offset: $offset" }
        
        try {
            // 1단계: 메시지 파싱
            val event = messageParser.parseCommunityNotificationEvent(message)
            logger.debug { "Parsed notification event for ${event.targetUserIds.size} users" }
            
            // 2단계: 알림 저장
            val savedNotifications = persistenceService.saveNotifications(event)
            logger.debug { "Saved ${savedNotifications.size} notifications" }
            
            // 3단계: 알림 전송
            deliveryService.deliverNotifications(event, savedNotifications)
            logger.debug { "Completed notification delivery" }
            
            // 4단계: 수동 커밋 (모든 처리 완료 후)
            acknowledgment?.acknowledge()
            
            logger.info { "Successfully processed community notification for ${event.targetUserIds.size} users" }
            
        } catch (e: MessageParser.MessageParsingException) {
            logger.error(e) { "Message parsing failed, skipping message: $message" }
            // 파싱 오류는 재시도해도 의미없으므로 ACK 처리
            acknowledgment?.acknowledge()
            
        } catch (e: NotificationPersistenceService.NotificationPersistenceException) {
            logger.error(e) { "Notification persistence failed for users: ${e.targetUserIds}" }
            // 영속성 오류는 재시도 가능하므로 ACK 하지 않음
            throw e
            
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error processing notification: $message" }
            // 예상치 못한 오류도 재시도 위해 ACK 하지 않음
            throw e
        }
    }
}