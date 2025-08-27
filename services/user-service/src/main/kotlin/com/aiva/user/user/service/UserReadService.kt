package com.aiva.user.user.service

import com.aiva.security.exception.UnauthorizedException
import com.aiva.user.user.dto.UserResponse
import com.aiva.user.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 사용자 조회 관련 비즈니스 로직 서비스
 * 사용자 정보 조회, 검색 등을 담당
 */
@Service
@Transactional(readOnly = true)
class UserReadService(
    private val userRepository: UserRepository
) {
    
    /**
     * 사용자 ID로 사용자 정보 조회
     */
    fun getUserInfo(userIdString: String): UserResponse {
        val userId = UUID.fromString(userIdString)
        val user = userRepository.findById(userId)
            .orElseThrow { UnauthorizedException("사용자를 찾을 수 없습니다") }
        
        return UserResponse.from(user)
    }
    
    /**
     * 사용자 존재 여부 확인
     */
    fun existsById(userId: UUID): Boolean {
        return userRepository.existsById(userId)
    }
    
    /**
     * 사용자 ID로 사용자 엔티티 조회
     */
    fun findById(userId: UUID) = userRepository.findById(userId)
}