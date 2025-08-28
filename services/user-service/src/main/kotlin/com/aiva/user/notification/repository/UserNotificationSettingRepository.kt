package com.aiva.user.notification.repository

import com.aiva.user.notification.entity.UserNotificationSetting
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserNotificationSettingRepository : JpaRepository<UserNotificationSetting, UUID> {
    /**
     * 사용자의 모든 권한 조회
     */
    fun findByUserId(userId: UUID): List<UserNotificationSetting>

    /**
     *  사용자 권한 조회
     */
    fun findByIdAndUserId(id: UUID, userId: UUID): UserNotificationSetting?
}
