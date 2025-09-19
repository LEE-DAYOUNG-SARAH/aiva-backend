package com.aiva.community.global.event.publisher

/**
 * 이벤트 발행 인터페이스
 * 
 * 의존성 역전 원칙을 적용하여 Kafka에 직접 의존하지 않고
 * 추상화된 인터페이스에 의존하도록 구현
 */
interface EventPublisher {
    
    /**
     * 이벤트를 비동기로 발행
     * 
     * @param topic 발행할 토픽
     * @param key 파티션 키
     * @param event 발행할 이벤트
     */
    fun publishAsync(topic: String, key: String, event: Any)
    
    /**
     * 이벤트를 동기로 발행 (테스트용)
     * 
     * @param topic 발행할 토픽
     * @param key 파티션 키
     * @param event 발행할 이벤트
     */
    fun publishSync(topic: String, key: String, event: Any)
}

/**
 * 이벤트 발행 결과
 */
data class PublishResult(
    val success: Boolean,
    val topic: String,
    val partition: Int? = null,
    val offset: Long? = null,
    val error: Throwable? = null
)