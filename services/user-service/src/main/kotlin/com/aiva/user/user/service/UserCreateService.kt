package com.aiva.user.user.service

import com.aiva.user.auth.dto.AppLoginRequest
import com.aiva.user.auth.dto.DeviceInfo
import com.aiva.user.auth.dto.UserInfo
import com.aiva.user.device.service.DeviceService
import com.aiva.user.user.entity.Provider
import com.aiva.user.user.entity.User
import com.aiva.user.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 사용자 생성 관련 비즈니스 로직 서비스
 * 사용자 등록, 계정 생성 등을 담당
 */
@Service
@Transactional
class UserCreateService(
    private val userRepository: UserRepository,
    private val deviceService: DeviceService,
) {
    
    /**
     * 사용자 생성/업데이트
     */
    fun findOrCreateUser(request: AppLoginRequest): User {
        val provider = Provider.valueOf(request.userInfo.provider.uppercase())
        
        val existingUser =
            userRepository.findByProviderAndProviderUserIdAndDeletedAtIsNull(provider, request.userInfo.providerUserId)

        return if(existingUser == null) {
            createUser(provider, request.userInfo, request.deviceInfo)
        } else {
            updateUser(existingUser, request.deviceInfo)
        }
    }
    /**
     * 사용자 생성
     */
    private fun createUser(provider: Provider, userInfo: UserInfo, deviceInfo: DeviceInfo): User {
        val user = userRepository.save(
            User(
                provider = provider,
                providerUserId = userInfo.providerUserId,
                email = userInfo.email,
                nickname = userInfo.nickname,
                avatarUrl = userInfo.avatarUrl
            )
        )

        deviceService.createDevice(user.id, deviceInfo)

        // TODO. 알림 설정 생

        return user
    }

    /**
     * 사용자 업데이트
     */
    private fun updateUser(user: User, deviceInfo: DeviceInfo): User {
        deviceService.updateDevice(user.id, deviceInfo)
        user.updateLastLogin()

        return user
    }
}