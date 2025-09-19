package com.aiva.common.redis.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import org.springframework.data.redis.core.index.Indexed
import java.time.LocalDateTime
import java.util.*

@RedisHash("subscription:cache")
data class SubscriptionCache(
    @Id
    val subscriptionId: String,
    
    val userId: UUID,
    
    val planId: UUID,
    
    val planName: String,
    
    @Indexed
    val status: SubscriptionStatus,
    
    val startDate: LocalDateTime,
    
    val endDate: LocalDateTime,
    
    val autoRenewal: Boolean = true,
    
    val price: Long, // 원화 기준 (원)
    
    val currency: String = "KRW",
    
    val lastPaymentDate: LocalDateTime? = null,
    
    val nextPaymentDate: LocalDateTime? = null,
    
    val createdAt: LocalDateTime,
    
    val updatedAt: LocalDateTime,
    
    @TimeToLive
    val ttl: Long = 30 * 60L // 30 minutes in seconds
) {
    enum class SubscriptionStatus {
        ACTIVE,
        EXPIRED,
        CANCELLED,
        PAUSED,
        PENDING_PAYMENT
    }
    
    fun isActive(): Boolean = status == SubscriptionStatus.ACTIVE && endDate.isAfter(LocalDateTime.now())
    
    fun isExpiringSoon(days: Long = 7): Boolean = 
        status == SubscriptionStatus.ACTIVE && 
        endDate.isBefore(LocalDateTime.now().plusDays(days))
    
    fun markAsExpired(): SubscriptionCache = copy(
        status = SubscriptionStatus.EXPIRED,
        updatedAt = LocalDateTime.now()
    )
    
    fun markAsCancelled(): SubscriptionCache = copy(
        status = SubscriptionStatus.CANCELLED,
        autoRenewal = false,
        updatedAt = LocalDateTime.now()
    )
    
    fun renewSubscription(newEndDate: LocalDateTime): SubscriptionCache = copy(
        endDate = newEndDate,
        lastPaymentDate = LocalDateTime.now(),
        nextPaymentDate = newEndDate.minusDays(1),
        updatedAt = LocalDateTime.now()
    )
}