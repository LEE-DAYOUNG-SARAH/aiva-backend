package com.aiva.security.util

import com.aiva.security.dto.UserPrincipal
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.*

/**
 * 보안 컨텍스트 유틸리티
 * 다른 서비스에서 Gateway가 전달한 사용자 정보를 쉽게 추출할 수 있도록 도움
 */
@Component
class SecurityContextUtil {
    
    /**
     * 현재 요청의 사용자 정보 가져오기
     */
    fun getCurrentUser(): UserPrincipal? {
        return try {
            val request = getCurrentRequest() ?: return null
            
            val userIdHeader = request.getHeader(UserPrincipal.USER_ID_HEADER)
            val emailHeader = request.getHeader(UserPrincipal.USER_EMAIL_HEADER)
            val nicknameHeader = request.getHeader(UserPrincipal.USER_NICKNAME_HEADER)
            
            if (userIdHeader != null && nicknameHeader != null) {
                UserPrincipal(
                    userId = UUID.fromString(userIdHeader),
                    email = emailHeader?.takeIf { it.isNotBlank() },
                    nickname = nicknameHeader
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 현재 사용자 ID 가져오기
     */
    fun getCurrentUserId(): UUID? {
        return getCurrentUser()?.userId
    }
    
    /**
     * 현재 사용자 이메일 가져오기
     */
    fun getCurrentUserEmail(): String? {
        return getCurrentUser()?.email
    }
    
    /**
     * 현재 사용자 닉네임 가져오기
     */
    fun getCurrentUserNickname(): String? {
        return getCurrentUser()?.nickname
    }
    
    /**
     * 사용자 인증 여부 확인
     */
    fun isAuthenticated(): Boolean {
        return getCurrentUser() != null
    }
    
    /**
     * 특정 사용자인지 확인
     */
    fun isCurrentUser(userId: UUID): Boolean {
        return getCurrentUserId() == userId
    }
    
    /**
     * 현재 HTTP 요청 가져오기
     */
    private fun getCurrentRequest(): HttpServletRequest? {
        return try {
            val requestAttributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?
            requestAttributes?.request
        } catch (e: Exception) {
            null
        }
    }
}