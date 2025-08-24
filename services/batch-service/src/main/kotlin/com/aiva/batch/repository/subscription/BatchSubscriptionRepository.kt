package com.aiva.batch.repository.subscription

import com.aiva.batch.entity.subscription.BatchUserSubscription
import com.aiva.batch.entity.subscription.SubscriptionStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

/**
 * 배치 처리용 Subscription Repository (읽기 전용)
 */
@Repository
interface BatchSubscriptionRepository : JpaRepository<BatchUserSubscription, UUID> {
    
    /**
     * 구독 기간이 만료된 활성 구독들 조회
     */
    @Query("""
        SELECT s FROM BatchUserSubscription s 
        WHERE s.status IN ('ACTIVE', 'TRIALING')
        AND s.currentPeriodEnd <= :currentTime
        AND s.cancelAtPeriodEnd = false
    """)
    fun findExpiredActiveSubscriptions(@Param("currentTime") currentTime: LocalDateTime): List<BatchUserSubscription>
    
    /**
     * 자동 갱신이 필요한 구독들 조회
     */
    @Query("""
        SELECT s FROM BatchUserSubscription s 
        WHERE s.status = 'ACTIVE'
        AND s.autoRenew = true
        AND s.nextBillingAt <= :currentTime
        AND s.cancelAtPeriodEnd = false
    """)
    fun findSubscriptionsForAutoRenewal(@Param("currentTime") currentTime: LocalDateTime): List<BatchUserSubscription>
    
    /**
     * 구독 만료 알림이 필요한 구독들 조회 (N일 후 만료)
     */
    @Query("""
        SELECT s FROM BatchUserSubscription s 
        WHERE s.status IN ('ACTIVE', 'TRIALING')
        AND s.currentPeriodEnd BETWEEN :startTime AND :endTime
        AND s.cancelAtPeriodEnd = false
    """)
    fun findSubscriptionsExpiringBetween(
        @Param("startTime") startTime: LocalDateTime,
        @Param("endTime") endTime: LocalDateTime
    ): List<BatchUserSubscription>
    
    /**
     * 특정 상태의 구독들 조회
     */
    @Query("""
        SELECT s FROM BatchUserSubscription s 
        WHERE s.status = :status
        AND s.updatedAt < :updatedBefore
    """)
    fun findByStatusAndUpdatedBefore(
        @Param("status") status: SubscriptionStatus,
        @Param("updatedBefore") updatedBefore: LocalDateTime
    ): List<BatchUserSubscription>
}