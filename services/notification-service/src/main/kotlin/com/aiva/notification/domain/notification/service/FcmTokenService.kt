package com.aiva.notification.domain.notification.service

import com.aiva.notification.consumer.FcmTokenDto
import com.aiva.notification.consumer.FcmTokenService
import com.aiva.notification.domain.fcm.repository.FcmTokenRepository
import com.aiva.notification.domain.device.repository.UserDeviceRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.*

@Service
class FcmTokenServiceImpl(
    private val fcmTokenRepository: FcmTokenRepository,
    private val userDeviceRepository: UserDeviceRepository
) : FcmTokenService {
    
    private val logger = KotlinLogging.logger {}
    
    override fun getActiveFcmTokensByUserIds(userIds: List<UUID>): List<FcmTokenDto> {
        return try {
            fcmTokenRepository.findByUserIdInAndIsActiveTrue(userIds)
                .map { fcmToken ->
                    val userDevice = userDeviceRepository.findById(fcmToken.userDeviceId).orElse(null)
                    FcmTokenDto(
                        userId = fcmToken.userId,
                        token = fcmToken.fcmToken,
                        deviceId = userDevice?.deviceIdentifier ?: "unknown",
                        isActive = fcmToken.isActive
                    )
                }
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to fetch FCM tokens for users: $userIds" }
            emptyList()
        }
    }
}