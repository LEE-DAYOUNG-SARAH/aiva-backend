package com.aiva.notification.device.repository

import com.aiva.notification.device.entity.FcmToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FcmTokenRepository : JpaRepository<FcmToken, UUID> {
    fun findByUserDeviceIdAndIsActiveTrue(userDeviceId: UUID): FcmToken?
    fun findByFcmToken(fcmToken: String): FcmToken?
    
    @Query("""
        SELECT ft FROM FcmToken ft 
        JOIN UserDevice ud ON ft.userDeviceId = ud.id 
        WHERE ud.userId IN :userIds AND ft.isActive = true
    """)
    fun findActiveFcmTokensByUserIds(@Param("userIds") userIds: List<UUID>): List<FcmToken>
    
    @Query("""
        SELECT new com.aiva.notification.consumer.FcmTokenDto(
            ud.userId, 
            ft.fcmToken, 
            ud.deviceIdentifier, 
            ft.isActive
        )
        FROM FcmToken ft 
        JOIN UserDevice ud ON ft.userDeviceId = ud.id 
        WHERE ud.userId IN :userIds AND ft.isActive = true
    """)
    fun findActiveFcmTokenDtosByUserIds(@Param("userIds") userIds: List<UUID>): List<com.aiva.notification.consumer.FcmTokenDto>
    
    @Query("""
        SELECT ft.fcmToken FROM FcmToken ft 
        JOIN UserDevice ud ON ft.userDeviceId = ud.id 
        WHERE ud.userId IN :userIds AND ft.isActive = true
    """)
    fun findActiveFcmTokenStringsByUserIds(@Param("userIds") userIds: List<UUID>): List<String>
}