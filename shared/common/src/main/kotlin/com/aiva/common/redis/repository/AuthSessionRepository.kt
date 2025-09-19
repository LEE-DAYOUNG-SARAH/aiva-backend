package com.aiva.common.redis.repository

import com.aiva.common.redis.entity.AuthSession
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AuthSessionRepository : CrudRepository<AuthSession, String> {
    
    fun findByUserId(userId: UUID): List<AuthSession>
    
    fun findByRefreshToken(refreshToken: String): AuthSession?
    
    fun deleteByUserId(userId: UUID)
    
    fun deleteByRefreshToken(refreshToken: String)
}