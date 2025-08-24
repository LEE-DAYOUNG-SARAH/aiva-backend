package com.aiva.batch.entity.subscription

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

/**
 * 배치 처리용 UserSubscription 엔티티 (읽기 전용)
 * Subscription Service의 user_subscriptions 테이블과 동일한 구조
 */
@Entity
@Table(name = "user_subscriptions")
data class BatchUserSubscription(
    @Id
    val id: UUID,
    
    @Column(name = "user_id", nullable = false)
    val userId: UUID,
    
    @Column(name = "plan_id", nullable = false)
    val planId: UUID,
    
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val status: SubscriptionStatus,
    
    @Column(name = "auto_renew", nullable = false)
    val autoRenew: Boolean = true,
    
    @Column(name = "started_at", nullable = false)
    val startedAt: LocalDateTime,
    
    @Column(name = "current_period_start", nullable = false)
    val currentPeriodStart: LocalDateTime,
    
    @Column(name = "current_period_end", nullable = false)
    val currentPeriodEnd: LocalDateTime,
    
    @Column(name = "next_billing_at")
    val nextBillingAt: LocalDateTime? = null,
    
    @Column(name = "cancel_at_period_end", nullable = false)
    val cancelAtPeriodEnd: Boolean = false,
    
    @Column(name = "canceled_at")
    val canceledAt: LocalDateTime? = null,
    
    @Column(name = "origin_order_id")
    val originOrderId: UUID? = null,
    
    @Column(name = "latest_order_id")
    val latestOrderId: UUID? = null,
    
    @Column(name = "usage_count", nullable = false)
    val usageCount: Int = 0,
    
    @Column(name = "usage_limit_per_period")
    val usageLimitPerPeriod: Int? = null,
    
    @Column(name = "usage_period_start", nullable = false)
    val usagePeriodStart: LocalDateTime,
    
    @Column(name = "usage_period_end", nullable = false)
    val usagePeriodEnd: LocalDateTime,
    
    @Column(name = "last_usage_at")
    val lastUsageAt: LocalDateTime? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime,
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime
)

enum class SubscriptionStatus {
    TRIALING, ACTIVE, PAST_DUE, CANCELED, EXPIRED, PAUSED
}