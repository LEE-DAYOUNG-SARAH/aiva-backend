package com.aiva.common.redis.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed
import java.time.LocalDate
import java.util.*

@RedisHash("child_cache")
data class ChildCache(
    @Id
    val id: UUID,
    
    @Indexed
    val userId: UUID,
    
    val isBorn: Boolean,
    
    val birthDate: LocalDate? = null,  // 출생일
    
    val gender: String,        // FEMALE, MALE, UNKNOWN
    
    // 캐시 관리용
    val cachedAt: String = java.time.LocalDateTime.now().toString()
)
