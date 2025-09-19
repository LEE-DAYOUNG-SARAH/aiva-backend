package com.aiva.common.redis.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.RedisTemplate

@AutoConfiguration
@ConditionalOnClass(RedisTemplate::class)
@Import(RedisConfig::class)
class RedisAutoConfiguration