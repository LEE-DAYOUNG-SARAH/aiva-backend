package com.aiva.common.kafka.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.util.backoff.FixedBackOff

/**
 * 공통 Kafka 설정
 * 
 * 모든 서비스에서 재사용 가능한 기본 Kafka 설정을 제공합니다.
 * 각 서비스는 이 설정을 상속하여 필요한 부분만 커스터마이징할 수 있습니다.
 */
@Configuration
@EnableKafka
@EnableConfigurationProperties(KafkaProperties::class)
@ConditionalOnProperty(prefix = "app.kafka", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class CommonKafkaConfig(
    private val kafkaProperties: KafkaProperties
) {
    
    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> {
        val props = buildConsumerProperties()
        return DefaultKafkaConsumerFactory(props)
    }
    
    @Bean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, String>
    ): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = consumerFactory
        
        // 컨테이너 설정
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        factory.containerProperties.isSyncCommits = true
        factory.setConcurrency(kafkaProperties.consumer.concurrency)
        
        // 공통 에러 핸들러
        factory.setCommonErrorHandler(createErrorHandler())
        
        return factory
    }
    
    @Bean
    fun producerFactory(): ProducerFactory<String, Any> {
        val props = buildProducerProperties()
        return DefaultKafkaProducerFactory(props)
    }
    
    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, Any>): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactory)
    }
    
    private fun buildConsumerProperties(): Map<String, Any> {
        return mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.bootstrapServers,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to kafkaProperties.consumer.autoOffsetReset,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to kafkaProperties.consumer.enableAutoCommit,
            ConsumerConfig.MAX_POLL_RECORDS_CONFIG to kafkaProperties.consumer.maxPollRecords,
            ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG to kafkaProperties.consumer.sessionTimeoutMs,
            ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG to kafkaProperties.consumer.heartbeatIntervalMs
        )
    }
    
    private fun buildProducerProperties(): Map<String, Any> {
        return mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to kafkaProperties.producer.acks,
            ProducerConfig.RETRIES_CONFIG to kafkaProperties.producer.retries,
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to kafkaProperties.producer.enableIdempotence,
            ProducerConfig.COMPRESSION_TYPE_CONFIG to kafkaProperties.producer.compressionType
        )
    }
    
    private fun createErrorHandler(): DefaultErrorHandler {
        // 3번 재시도, 5초 간격
        val backOff = FixedBackOff(5000L, 3L)
        return DefaultErrorHandler(backOff)
    }
}