package com.aiva.user.device.repository

import com.aiva.user.device.entity.FcmToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FcmTokenRepository : JpaRepository<FcmToken, UUID> {
    fun findByUserDeviceIdAndIsActiveTrue(userDeviceId: UUID): FcmToken?
    fun findByFcmToken(fcmToken: String): FcmToken?
}
