package com.aiva.notification.device.repository

import com.aiva.notification.device.entity.UserDevice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserDeviceRepository : JpaRepository<UserDevice, UUID> {
    fun findByUserIdAndDeviceIdentifierAndDeletedAtIsNull(userId: UUID, deviceIdentifier: String): UserDevice?
    
    @Query("""
        SELECT DISTINCT ud.userId FROM UserDevice ud 
        JOIN FcmToken ft ON ud.id = ft.userDeviceId 
        WHERE ft.isActive = true AND ud.userId IN :userIds
    """)
    fun findActiveUserIdsByUserIds(@Param("userIds") userIds: List<UUID>): List<UUID>
}