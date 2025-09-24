package com.aiva.common.redis.repository

import com.aiva.common.redis.entity.ActiveChatStream
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ActiveChatStreamRepository : CrudRepository<ActiveChatStream, String> {
    
    fun findByChatId(chatId: UUID): List<ActiveChatStream>
    
    fun findByUserId(userId: UUID): List<ActiveChatStream>
    
    fun deleteByUserId(userId: UUID)
}