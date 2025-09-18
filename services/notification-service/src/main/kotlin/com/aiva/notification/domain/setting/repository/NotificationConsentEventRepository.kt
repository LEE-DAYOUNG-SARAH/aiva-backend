package com.aiva.notification.domain.setting.repository

import com.aiva.notification.domain.setting.entity.NotificationConsentEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface NotificationConsentEventRepository : JpaRepository<NotificationConsentEvent, UUID> {
    fun findByUserId(userId: UUID): List<NotificationConsentEvent>
}