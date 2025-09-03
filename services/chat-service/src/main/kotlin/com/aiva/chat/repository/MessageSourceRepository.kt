package com.aiva.chat.repository

import com.aiva.chat.entity.MessageSource
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MessageSourceRepository : JpaRepository<MessageSource, UUID> {
    fun findByMessageId(messageId: UUID): List<MessageSource>
    fun findBySourceTitle(sourceTitle: String): List<MessageSource>
}