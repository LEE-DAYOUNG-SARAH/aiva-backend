package com.aiva.common.redis.service

import com.aiva.common.redis.entity.AuthSession
import com.aiva.common.redis.repository.AuthSessionRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class RedisAuthService(
    private val authSessionRepository: AuthSessionRepository
) {
    
    fun createSession(
        userId: UUID,
        refreshToken: String,
        deviceInfo: String? = null,
        ipAddress: String? = null,
        userAgent: String? = null
    ): AuthSession {
        val sessionId = UUID.randomUUID().toString()
        val session = AuthSession(
            sessionId = sessionId,
            userId = userId,
            refreshToken = refreshToken,
            deviceInfo = deviceInfo,
            ipAddress = ipAddress,
            userAgent = userAgent
        )
        return authSessionRepository.save(session)
    }
    
    fun findSession(sessionId: String): AuthSession? {
        return authSessionRepository.findById(sessionId).orElse(null)
    }
    
    fun findSessionByRefreshToken(refreshToken: String): AuthSession? {
        return authSessionRepository.findByRefreshToken(refreshToken)
    }
    
    fun findUserSessions(userId: UUID): List<AuthSession> {
        return authSessionRepository.findByUserId(userId)
    }
    
    fun updateLastAccessed(sessionId: String): AuthSession? {
        return authSessionRepository.findById(sessionId).orElse(null)?.let { session ->
            val updatedSession = session.copy(lastAccessedAt = LocalDateTime.now())
            authSessionRepository.save(updatedSession)
        }
    }
    
    fun deleteSession(sessionId: String) {
        authSessionRepository.deleteById(sessionId)
    }
    
    fun deleteSessionByRefreshToken(refreshToken: String) {
        authSessionRepository.deleteByRefreshToken(refreshToken)
    }
    
    fun deleteAllUserSessions(userId: UUID) {
        authSessionRepository.deleteByUserId(userId)
    }
    
    fun deleteOtherUserSessions(userId: UUID, currentSessionId: String) {
        val allSessions = authSessionRepository.findByUserId(userId)
        allSessions.filter { it.sessionId != currentSessionId }
            .forEach { authSessionRepository.deleteById(it.sessionId) }
    }
}