package com.aiva.common.redis

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

/**
 * Redis 설정
 * 모든 서비스에서 동일한 Redis 설정 사용
 */
@Configuration
@EnableRedisRepositories(basePackages = ["com.aiva.common.redis.repository"])
class RedisConfig {
    
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, String> {
        val template = RedisTemplate<String, String>()
        template.connectionFactory = connectionFactory
        
        // 키와 값 모두 String 직렬화 사용
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = StringRedisSerializer()
        
        template.afterPropertiesSet()
        return template
    }
}