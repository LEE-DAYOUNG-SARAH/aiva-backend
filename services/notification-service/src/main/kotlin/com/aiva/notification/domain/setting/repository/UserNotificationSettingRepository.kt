package com.aiva.notification.setting.repository

import com.aiva.notification.setting.entity.UserNotificationSetting
import com.aiva.notification.setting.entity.NotificationPermissionType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserNotificationSettingRepository : JpaRepository<UserNotificationSetting, UUID> {
    /**
     * 사용자의 모든 권한 조회
     */
    fun findByUserId(userId: UUID): List<UserNotificationSetting>

    /**
     * 사용자 권한 조회
     */
    fun findByIdAndUserId(id: UUID, userId: UUID): UserNotificationSetting?
    
    /**
     * 사용자별 특정 권한 타입 조회
     */
    fun findByUserIdAndPermissionType(userId: UUID, permissionType: NotificationPermissionType): UserNotificationSetting?
    
    /**
     * 여러 사용자의 특정 권한 타입이 활성화된 사용자 ID들 조회
     */
    @Query("""
        SELECT uns.userId FROM UserNotificationSetting uns
        WHERE uns.userId IN :userIds 
        AND uns.permissionType = :permissionType 
        AND uns.isEnabled = true
    """)
    fun findEnabledUserIdsByUserIdsAndPermissionType(
        @Param("userIds") userIds: List<UUID>,
        @Param("permissionType") permissionType: NotificationPermissionType
    ): List<UUID>
}