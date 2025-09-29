package com.aiva.common.redis.repository

import com.aiva.common.redis.entity.ChatSession
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ChatSessionRepository : CrudRepository<ChatSession, String> {
    
    fun findByUserId(userId: UUID): List<ChatSession>
    
    fun deleteByUserId(userId: UUID)
}