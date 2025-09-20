package com.aiva.community.global.event

import com.aiva.community.domain.user.UserProfileProjection
import com.aiva.community.domain.user.UserProfileProjectionRepository
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * User 서비스의 프로필 변경 이벤트를 처리하는 Kafka 컨슈머
 * 
 * 핵심 기능:
 * 1. 이벤트 순서 보장 (version 기반)
 * 2. Upsert 처리 (없으면 생성, 있으면 업데이트)
 * 3. 실패 시 재시도 및 DLQ 처리
 */
@Service
class UserProfileChangedConsumer    (
    private val userProfileRepository: UserProfileProjectionRepository,
    private val objectMapper: ObjectMapper
) {
    
    private val logger = KotlinLogging.logger {}
    
    @KafkaListener(
        topics = ["\${app.kafka.topics.user-profile-changed.name:user.profile.changed}"],
        groupId = "\${app.kafka.topics.user-profile-changed.consumer-group:community-service-group}",
        containerFactory = "kafkaListenerContainerFactory",
        errorHandler = "kafkaErrorHandler"
    )
    @Transactional
    fun handleUserProfileChanged(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment?
    ) {
        try {
            logger.debug { "Received user profile changed event from topic: $topic, partition: $partition, offset: $offset" }
            
            val event = objectMapper.readValue(message, UserProfileChangedEvent::class.java)
            
            when (event.eventType) {
                UserProfileEventType.CREATED,
                UserProfileEventType.UPDATED -> handleUpsert(event)
                UserProfileEventType.DELETED -> handleDelete(event)
            }
            
            // 수동 커밋 (처리 완료 후)
            acknowledgment?.acknowledge()
            
            logger.info { "Successfully processed user profile event for user: ${event.userId}, version: ${event.version}" }
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to process user profile changed event: $message" }
            // 예외 발생 시 acknowledge 하지 않음 -> 재시도 또는 DLQ로 이동
            throw e
        }
    }
    
    /**
     * 사용자 프로필 Upsert 처리
     */
    private fun handleUpsert(event: UserProfileChangedEvent) {
        logger.debug { "Processing upsert for user: ${event.userId}, version: ${event.version}" }
        
        // 1. 기존 프로젝션 조회
        val existing = userProfileRepository.findById(event.userId).orElse(null)
        
        if (existing != null) {
            // 2. 순서 보장 체크
            if (event.version <= existing.version) {
                logger.warn { "Ignoring out-of-order event for user: ${event.userId}, eventVersion: ${event.version}, currentVersion: ${existing.version}" }
                return
            }
            
            // 3. 기존 엔티티 업데이트
            val updated = existing.updateFromEvent(
                nickname = event.nickname,
                avatarUrl = event.avatarUrl,
                level = event.level,
                version = event.version,
                updatedAt = event.updatedAt
            )
            
            if (updated) {
                userProfileRepository.save(existing)
                logger.debug { "Updated existing user profile for user: ${event.userId}" }
            }
        } else {
            // 4. 새 프로젝션 생성
            val newProjection = UserProfileProjection(
                userId = event.userId,
                nickname = event.nickname,
                avatarUrl = event.avatarUrl,
                level = event.level,
                version = event.version,
                updatedAt = event.updatedAt
            )
            
            userProfileRepository.save(newProjection)
            logger.debug { "Created new user profile projection for user: ${event.userId}" }
        }
    }
    
    /**
     * 사용자 프로필 삭제 처리
     */
    private fun handleDelete(event: UserProfileChangedEvent) {
        logger.debug { "Processing delete for user: ${event.userId}" }
        
        val existing = userProfileRepository.findById(event.userId).orElse(null)
        if (existing != null && event.version > existing.version) {
            userProfileRepository.deleteById(event.userId)
            logger.info { "Deleted user profile projection for user: ${event.userId}" }
        } else {
            logger.warn { "Cannot delete user profile: user not found or out-of-order event for user: ${event.userId}" }
        }
    }
}