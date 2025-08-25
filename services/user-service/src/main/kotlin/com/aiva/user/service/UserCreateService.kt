package com.aiva.user.service

import com.aiva.user.dto.AppLoginRequest
import com.aiva.user.entity.Provider
import com.aiva.user.entity.User
import com.aiva.user.repository.UserRepository
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
     * 앱에서 받은 사용자 정보로 기존 사용자 조회 또는 신규 생성
     */
    fun findOrCreateUser(appUser: AppLoginRequest): User {
        val provider = Provider.valueOf(appUser.provider.uppercase())
        
        return userRepository.findByProviderAndProviderUserIdAndDeletedAtIsNull(provider, appUser.providerUserId)
            ?: userRepository.save(
                User(
                    provider = provider,
                    providerUserId = appUser.providerUserId,
                    email = appUser.email,
                    nickname = appUser.nickname,
                    avatarUrl = appUser.avatarUrl
                )
            )
    }
}