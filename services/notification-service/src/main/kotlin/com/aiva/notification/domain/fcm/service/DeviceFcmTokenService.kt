package com.aiva.notification.domain.fcm.service

import com.aiva.notification.domain.fcm.dto.FcmTokenUpdateResponse
import com.aiva.notification.domain.fcm.dto.FcmTokenUpdateRequest
import com.aiva.notification.domain.fcm.entity.FcmToken
import com.aiva.notification.domain.device.entity.UserDevice
import com.aiva.notification.domain.device.service.DeviceService
import com.aiva.notification.domain.fcm.repository.FcmTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class DeviceFcmTokenService(
    private val deviceService: DeviceService,
    private val fcmTokenRepository: FcmTokenRepository
) {
    
    /**
     * FCM 토큰 업데이트
     */
    fun updateFcmToken(userId: UUID, deviceIdentifier: String, request: FcmTokenUpdateRequest): FcmTokenUpdateResponse {
        // 1. 디바이스 조회 및 권한 확인
        val device = deviceService.getUserDevice(userId, deviceIdentifier)
            ?: throw IllegalArgumentException("디바이스를 찾을 수 없습니다")
        
        // 2. 기존 토큰 조회
        val existingToken = fcmTokenRepository.findByUserDeviceIdAndIsActiveTrue(device.id)
        
        // 3. 동일한 토큰인지 확인
        return if (isSameToken(existingToken, request.fcmToken)) {
            // 기존 토큰 검증 시간 업데이트
            updateTokenValidationTime(existingToken!!)
        } else {
            // 새 토큰으로 교체
            replaceWithNewToken(device, existingToken, request.fcmToken)
        }
    }

    /**
     * 동일한 토큰인지 확인
     */
    private fun isSameToken(existingToken: FcmToken?, newToken: String): Boolean {
        return existingToken != null && existingToken.fcmToken == newToken
    }

    /**
     * 기존 토큰 검증 시간 업데이트
     */
    private fun updateTokenValidationTime(token: FcmToken): FcmTokenUpdateResponse {
        token.updateLastValidateAt()

        return FcmTokenUpdateResponse.from(token)
    }

    /**
     * 새 토큰으로 교체
     */
    private fun replaceWithNewToken(
        device: UserDevice,
        existingToken: FcmToken?,
        newToken: String
    ): FcmTokenUpdateResponse {
        // 1. 기존 토큰 비활성화
        existingToken?.revoke()

        // 2. 다른 디바이스의 중복 토큰 비활성화
        fcmTokenRepository.findByFcmToken(newToken)?.let { duplicateToken ->
            if (duplicateToken.isActive) {
                duplicateToken.revoke()
            }
        }

        // 3. 새 토큰 등록
        val newFcmToken = fcmTokenRepository.save(
            FcmToken(
                userDeviceId = device.id,
                userId = device.userId,
                fcmToken = newToken,
                lastValidatedAt = LocalDateTime.now()
            )
        )

        // 4. 디바이스 접속 시간 업데이트
        device.updateLastSeen()

        return FcmTokenUpdateResponse.from(newFcmToken)
    }

}