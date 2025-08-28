package com.aiva.user.device.service

import com.aiva.user.auth.dto.DeviceInfo
import com.aiva.user.device.dto.DeviceUpdateResponse
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
     * 디바이스 생성
     */
    fun createDevice(userId: UUID, request: DeviceInfo) {
        userDeviceRepository.save(
            UserDevice(
                userId = userId,
                deviceIdentifier = request.deviceIdentifier,
                platform = Platform.valueOf(request.platform.uppercase()),
                appVersion = request.appVersion
            )
        )
    }

    /**
     * 디바이스 업데이트
     */
    fun updateDevice(userId: UUID, deviceInfo: DeviceInfo) {
        val existingDevice = getUserDevice(userId, deviceInfo.deviceIdentifier)

        if(existingDevice != null) {
            existingDevice.updateAppVersion(deviceInfo.appVersion)
        } else {
            createDevice(userId, deviceInfo)
        }
    }
    
    /**
     * 디바이스 상세 정보 업데이트
     */
    fun updateDeviceDetails(userId: UUID, deviceIdentifier: String, request: DeviceUpdateRequest): DeviceUpdateResponse {
        val device = getUserDevice(userId, deviceIdentifier)
            ?: throw UnauthorizedException("디바이스를 찾을 수 없습니다")
        
        device.updateDeviceDetails(request.deviceModel, request.osVersion)

        return DeviceUpdateResponse(
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
