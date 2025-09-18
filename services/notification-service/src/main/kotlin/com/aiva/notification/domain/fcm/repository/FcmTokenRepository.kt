package com.aiva.notification.domain.fcm.repository

import com.aiva.notification.domain.fcm.entity.FcmToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FcmTokenRepository : JpaRepository<FcmToken, UUID> {
    fun findByUserDeviceIdAndIsActiveTrue(userDeviceId: UUID): FcmToken?
    fun findByFcmToken(fcmToken: String): FcmToken?
    
    fun findByUserIdInAndIsActiveTrue(userIds: List<UUID>): List<FcmToken>
    
    @Query("""
        SELECT ft.fcmToken FROM FcmToken ft 
        JOIN UserDevice ud ON ft.userDeviceId = ud.id 
        WHERE ud.userId IN :userIds AND ft.isActive = true
    """)
    fun findActiveFcmTokenStringsByUserIds(@Param("userIds") userIds: List<UUID>): List<String>
}