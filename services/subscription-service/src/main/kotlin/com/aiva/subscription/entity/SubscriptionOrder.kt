package com.aiva.subscription.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "subscription_orders")
@EntityListeners(AuditingEntityListener::class)
data class SubscriptionOrder(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "user_id", nullable = false)
    val userId: UUID,
    
    @Column(name = "plan_id", nullable = false)
    val planId: UUID,
    
    @Column(name = "order_no", nullable = false, unique = true, length = 50)
    val orderNo: String,
    
    @Column(name = "order_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val orderType: OrderType,
    
    @Column(name = "amount", nullable = false)
    val amount: Int,
    
    @Column(name = "currency", nullable = false, length = 10)
    val currency: String = "KRW",
    
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    var status: OrderStatus,
    
    @Column(name = "requested_at", nullable = false)
    val requestedAt: LocalDateTime,
    
    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null,
    
    @Column(name = "canceled_at")
    var canceledAt: LocalDateTime? = null,
    
    @Column(name = "failure_reason", length = 300)
    var failureReason: String? = null,
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @OneToMany(mappedBy = "orderId", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val payments: List<Payment> = mutableListOf()
)

@Entity
@Table(name = "payments")
@EntityListeners(AuditingEntityListener::class)
data class Payment(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "order_id", nullable = false)
    val orderId: UUID,
    
    @Column(name = "provider", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    val provider: PaymentProvider,
    
    @Column(name = "method", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val method: PaymentMethod,
    
    @Column(name = "external_id", length = 100)
    val externalId: String? = null,
    
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    var status: PaymentStatus,
    
    @Column(name = "amount", nullable = false)
    val amount: Int,
    
    @Column(name = "currency", nullable = false, length = 10)
    val currency: String = "KRW",
    
    @Column(name = "approved_at")
    var approvedAt: LocalDateTime? = null,
    
    @Column(name = "failed_at")
    var failedAt: LocalDateTime? = null,
    
    @Column(name = "refunded_at")
    var refundedAt: LocalDateTime? = null,
    
    @Column(name = "card_last4", length = 4)
    val cardLast4: String? = null,
    
    @Column(name = "receipt_url", length = 300)
    val receiptUrl: String? = null,
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "subscription_cancellations")
@EntityListeners(AuditingEntityListener::class)
data class SubscriptionCancellation(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "user_subscription_id", nullable = false)
    val userSubscriptionId: UUID,
    
    @Column(name = "canceled_at", nullable = false)
    val canceledAt: LocalDateTime,
    
    @Column(name = "effective_at", nullable = false)
    val effectiveAt: LocalDateTime,
    
    @Column(name = "initiator", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    val initiator: CancellationInitiator,
    
    @Column(name = "reason_code", length = 30)
    val reasonCode: String? = null,
    
    @Column(name = "reason_text", length = 500)
    val reasonText: String? = null,
    
    @Column(name = "refund_payment_id")
    val refundPaymentId: UUID? = null,
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class OrderType {
    NEW, RENEWAL, UPGRADE, DOWNGRADE
}

enum class OrderStatus {
    PENDING, COMPLETED, CANCELED, FAILED
}

enum class PaymentProvider {
    TOSS_PAYMENTS, IAMPORT, APPLE, GOOGLE
}

enum class PaymentMethod {
    CARD, BANK, IAP
}

enum class PaymentStatus {
    PENDING, AUTHORIZED, PAID, FAILED, REFUNDED, PARTIAL_REFUNDED
}

enum class CancellationInitiator {
    USER, ADMIN, SYSTEM
}
