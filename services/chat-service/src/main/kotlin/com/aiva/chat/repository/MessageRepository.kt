package com.aiva.chat.repository

import com.aiva.chat.entity.Message
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MessageRepository : JpaRepository<Message, UUID> {
    
    @Query("SELECT m FROM Message m WHERE m.chatId = :chatId ORDER BY m.createdAt ASC")
    fun findByChatIdOrderByCreatedAtAsc(@Param("chatId") chatId: UUID): List<Message>
    
    @Query("SELECT m FROM Message m WHERE m.chatId = :chatId ORDER BY m.createdAt DESC LIMIT 1")
    fun findLastMessageByChatId(@Param("chatId") chatId: UUID): Message?
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.chatId = :chatId")
    fun countMessagesByChatId(@Param("chatId") chatId: UUID): Long
    
    fun countByChatId(chatId: UUID): Long
}