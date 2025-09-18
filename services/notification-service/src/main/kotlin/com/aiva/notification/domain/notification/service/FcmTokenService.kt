package com.aiva.notification.service

import com.aiva.notification.consumer.FcmTokenDto
import com.aiva.notification.consumer.FcmTokenService
import com.aiva.notification.device.repository.FcmTokenRepository
import com.aiva.notification.device.repository.UserDeviceRepository
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
            // 로컬 DB에서 FCM 토큰 조회 (JOIN으로 한 번에 처리)
            fcmTokenRepository.findActiveFcmTokenDtosByUserIds(userIds)
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to fetch FCM tokens for users: $userIds" }
            emptyList()
        }
    }
}