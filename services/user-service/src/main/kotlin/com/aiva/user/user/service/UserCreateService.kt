package com.aiva.user.user.service

import com.aiva.user.auth.dto.AppLoginRequest
import com.aiva.user.auth.dto.DeviceInfo
import com.aiva.user.auth.dto.UserInfo
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
    private val userRepository: UserRepository
) {
    
    /**
     * 사용자 생성/업데이트
     */
    fun findOrCreateUser(request: AppLoginRequest): User {
        val provider = Provider.valueOf(request.userInfo.provider.uppercase())
        
        val existingUser =
            userRepository.findByProviderAndProviderUserIdAndDeletedAtIsNull(provider, request.userInfo.providerUserId)

        return if(existingUser == null) {
            createUser(provider, request.userInfo, request.deviceInfo, request.systemNotificationEnabled)
        } else {
            updateUser(existingUser, request.deviceInfo)
        }
    }
    /**
     * 사용자 생성
     */
    private fun createUser(
        provider: Provider,
        userInfo: UserInfo,
        deviceInfo: DeviceInfo,
        systemNotificationEnabled: Boolean?
    ): User {
        val user = userRepository.save(
            User(
                provider = provider,
                providerUserId = userInfo.providerUserId,
                email = userInfo.email,
                nickname = userInfo.nickname,
                avatarUrl = userInfo.avatarUrl
            )
        )

        // TODO: 디바이스 정보는 notification-service에서 별도 처리 (Kafka 이벤트 또는 클라이언트 호출)

        // TODO: 알림 설정은 notification-service에서 별도 처리

        return user
    }

    /**
     * 사용자 업데이트
     */
    private fun updateUser(user: User, deviceInfo: DeviceInfo): User {
        // TODO: 디바이스 정보 업데이트는 notification-service에서 별도 처리
        user.updateLastLogin()

        return user
    }
}