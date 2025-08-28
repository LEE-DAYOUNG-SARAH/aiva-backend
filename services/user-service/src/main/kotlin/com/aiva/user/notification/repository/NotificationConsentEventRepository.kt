package com.aiva.user.notification.repository

import com.aiva.user.notification.entity.NotificationConsentEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface NotificationConsentEventRepository : JpaRepository<NotificationConsentEvent, UUID> {
    fun findByUserId(userId: UUID): List<NotificationConsentEvent>
}
