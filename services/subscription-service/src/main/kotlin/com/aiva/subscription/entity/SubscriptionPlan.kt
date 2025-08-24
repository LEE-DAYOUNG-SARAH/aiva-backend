package com.aiva.subscription.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "subscription_plans")
@EntityListeners(AuditingEntityListener::class)
data class SubscriptionPlan(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "code", nullable = false, unique = true, length = 50)
    val code: String,
    
    @Column(name = "name", nullable = false, length = 100)
    val name: String,
    
    @Column(name = "description", length = 500)
    val description: String? = null,
    
    @Column(name = "billing_period_unit", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    val billingPeriodUnit: BillingPeriodUnit,
    
    @Column(name = "billing_period_count", nullable = false)
    val billingPeriodCount: Int,
    
    @Column(name = "price_amount", nullable = false)
    val priceAmount: Int,
    
    @Column(name = "currency", nullable = false, length = 10)
    val currency: String = "KRW",
    
    @Column(name = "trial_days", nullable = false)
    val trialDays: Int = 0,
    
    @Column(name = "usage_limit_per_period")
    val usageLimitPerPeriod: Int? = null,
    
    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "user_subscriptions")
@EntityListeners(AuditingEntityListener::class)
data class UserSubscription(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "user_id", nullable = false, unique = true)
    val userId: UUID,
    
    @Column(name = "plan_id", nullable = false)
    val planId: UUID,
    
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    var status: SubscriptionStatus,
    
    @Column(name = "auto_renew", nullable = false)
    var autoRenew: Boolean = true,
    
    @Column(name = "started_at", nullable = false)
    val startedAt: LocalDateTime,
    
    @Column(name = "current_period_start", nullable = false)
    var currentPeriodStart: LocalDateTime,
    
    @Column(name = "current_period_end", nullable = false)
    var currentPeriodEnd: LocalDateTime,
    
    @Column(name = "next_billing_at")
    var nextBillingAt: LocalDateTime? = null,
    
    @Column(name = "cancel_at_period_end", nullable = false)
    var cancelAtPeriodEnd: Boolean = false,
    
    @Column(name = "canceled_at")
    var canceledAt: LocalDateTime? = null,
    
    @Column(name = "origin_order_id")
    val originOrderId: UUID? = null,
    
    @Column(name = "latest_order_id")
    var latestOrderId: UUID? = null,
    
    @Column(name = "usage_count", nullable = false)
    var usageCount: Int = 0,
    
    @Column(name = "usage_limit_per_period")
    var usageLimitPerPeriod: Int? = null,
    
    @Column(name = "usage_period_start", nullable = false)
    var usagePeriodStart: LocalDateTime,
    
    @Column(name = "usage_period_end", nullable = false)
    var usagePeriodEnd: LocalDateTime,
    
    @Column(name = "last_usage_at")
    var lastUsageAt: LocalDateTime? = null,
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun incrementUsage() {
        usageCount++
        lastUsageAt = LocalDateTime.now()
    }
    
    fun resetUsage() {
        usageCount = 0
    }
    
    fun canUse(): Boolean {
        return status == SubscriptionStatus.ACTIVE && 
               (usageLimitPerPeriod == null || usageCount < usageLimitPerPeriod!!)
    }
}

enum class BillingPeriodUnit {
    MONTH, YEAR
}

enum class SubscriptionStatus {
    TRIALING,
    ACTIVE,
    PAST_DUE,
    CANCELED,
    EXPIRED,
    PAUSED
}
