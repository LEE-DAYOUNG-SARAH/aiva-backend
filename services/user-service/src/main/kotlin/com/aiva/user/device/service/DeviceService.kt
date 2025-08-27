package com.aiva.user.device.service

import com.aiva.user.auth.dto.DeviceInfo
import com.aiva.user.device.dto.DeviceResponse
import com.aiva.user.device.dto.DeviceUpdateRequest
import com.aiva.user.device.entity.Platform
import com.aiva.user.device.entity.UserDevice
import com.aiva.user.device.repository.UserDeviceRepository
import com.aiva.security.exception.UnauthorizedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class DeviceService(
    private val userDeviceRepository: UserDeviceRepository
) {
    
    /**
     * 로그인 시 디바이스 정보 등록/업데이트
     */
    fun registerOrUpdateDevice(userId: UUID, deviceInfo: DeviceInfo): UserDevice {
        val platform = Platform.valueOf(deviceInfo.platform.uppercase())
        
        val existingDevice = getUserDevice(userId, deviceInfo.deviceIdentifier)
        
        return if (existingDevice != null) {
            // 기존 디바이스 앱 버전 업데이트
            existingDevice.updateAppVersion(deviceInfo.appVersion)
            existingDevice
        } else {
            // 새 디바이스 등록
            userDeviceRepository.save(
                UserDevice(
                    userId = userId,
                    deviceIdentifier = deviceInfo.deviceIdentifier,
                    platform = platform,
                    appVersion = deviceInfo.appVersion
                )
            )
        }
    }
    
    /**
     * 디바이스 상세 정보 업데이트
     */
    fun updateDeviceDetails(userId: UUID, deviceIdentifier: String, request: DeviceUpdateRequest): DeviceResponse {
        val device = getUserDevice(userId, deviceIdentifier)
            ?: throw UnauthorizedException("디바이스를 찾을 수 없습니다")
        
        device.updateDeviceDetails(request.deviceModel, request.osVersion)

        return DeviceResponse(
            id = device.id.toString(),
            deviceIdentifier = device.deviceIdentifier,
            platform = device.platform.name,
            deviceModel = device.deviceModel,
            osVersion = device.osVersion,
            appVersion = device.appVersion,
            lastSeenAt = device.lastSeenAt.toString(),
            createdAt = device.createdAt.toString()
        )
    }

    @Transactional(readOnly = true)
    fun getUserDevice(userId: UUID, deviceIdentifier: String): UserDevice? {
        return userDeviceRepository.findByUserIdAndDeviceIdentifierAndDeletedAtIsNull(userId, deviceIdentifier)
    }
}
