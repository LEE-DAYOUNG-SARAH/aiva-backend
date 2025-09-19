package com.aiva.common.kafka.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Kafka 설정 프로퍼티
 * 
 * 모든 서비스에서 공통으로 사용할 Kafka 설정을 중앙집중 관리
 */
@ConfigurationProperties(prefix = "app.kafka")
data class KafkaProperties(
    val bootstrapServers: String = "localhost:9092",
    val consumer: ConsumerProperties = ConsumerProperties(),
    val producer: ProducerProperties = ProducerProperties(),
    val topics: Map<String, TopicProperties> = emptyMap()
) {
    
    data class ConsumerProperties(
        val autoOffsetReset: String = "latest",
        val enableAutoCommit: Boolean = false,
        val maxPollRecords: Int = 10,
        val sessionTimeoutMs: Int = 30000,
        val heartbeatIntervalMs: Int = 10000,
        val concurrency: Int = 2
    )
    
    data class ProducerProperties(
        val acks: String = "all",
        val retries: Int = 3,
        val enableIdempotence: Boolean = true,
        val compressionType: String = "lz4"
    )
    
    data class TopicProperties(
        val name: String,
        val consumerGroup: String,
        val partitions: Int = 3,
        val replicationFactor: Short = 1
    )
}