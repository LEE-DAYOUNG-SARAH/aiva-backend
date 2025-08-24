package com.aiva.batch.repository.notification

import com.aiva.batch.entity.notification.BatchNotification
import com.aiva.batch.entity.notification.BatchNotificationRecipient
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

/**
 * 배치 처리용 Notification Repository (읽기 전용)
 */
@Repository
interface BatchNotificationRepository : JpaRepository<BatchNotification, UUID> {
    
    /**
     * 예약된 시간이 된 알림들 조회
     */
    @Query("""
        SELECT n FROM BatchNotification n 
        WHERE n.scheduledAt <= :currentTime
        AND n.scheduledAt IS NOT NULL
    """)
    fun findScheduledNotificationsToSend(@Param("currentTime") currentTime: LocalDateTime): List<BatchNotification>
    
    /**
     * 오래된 알림들 조회 (N일 이상 된 알림)
     */
    @Query("""
        SELECT n FROM BatchNotification n 
        WHERE n.createdAt < :cutoffDate
    """)
    fun findOldNotifications(@Param("cutoffDate") cutoffDate: LocalDateTime): List<BatchNotification>
}

@Repository
interface BatchNotificationRecipientRepository : JpaRepository<BatchNotificationRecipient, UUID> {
    
    /**
     * 특정 알림의 수신자들 조회
     */
    @Query("""
        SELECT nr FROM BatchNotificationRecipient nr 
        WHERE nr.notificationId = :notificationId
    """)
    fun findByNotificationId(@Param("notificationId") notificationId: UUID): List<BatchNotificationRecipient>
    
    /**
     * 읽지 않은 오래된 알림 수신자들 조회
     */
    @Query("""
        SELECT nr FROM BatchNotificationRecipient nr 
        WHERE nr.isRead = false
        AND nr.createdAt < :cutoffDate
    """)
    fun findUnreadOldRecipients(@Param("cutoffDate") cutoffDate: LocalDateTime): List<BatchNotificationRecipient>
    
    /**
     * 특정 사용자의 알림 수신자 데이터 조회
     */
    @Query("""
        SELECT nr FROM BatchNotificationRecipient nr 
        WHERE nr.userId = :userId
        AND nr.createdAt < :cutoffDate
    """)
    fun findByUserIdAndCreatedBefore(
        @Param("userId") userId: UUID,
        @Param("cutoffDate") cutoffDate: LocalDateTime
    ): List<BatchNotificationRecipient>
}