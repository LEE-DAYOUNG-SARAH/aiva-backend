package com.aiva.chat.service

import com.aiva.chat.dto.ChatCreateResponse
import com.aiva.chat.entity.Chat
import com.aiva.chat.entity.Message
import com.aiva.chat.entity.MessageRole
import com.aiva.chat.entity.MessageSource
import com.aiva.chat.repository.ChatRepository
import com.aiva.chat.repository.MessageRepository
import com.aiva.chat.repository.MessageSourceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class ChatManagementService(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val messageSourceRepository: MessageSourceRepository
) {
    
    @Transactional
    fun createNewChat(userId: UUID): ChatCreateResponse {
        // 채팅방 생성
        val chat = chatRepository.save(
            Chat(userId = userId)
        )
        
        return ChatCreateResponse(chat.id)
    }
    
    @Transactional
    fun saveAiMessage(chatId: UUID, content: String): Message {
        val assistantMessage = Message(
            chatId = chatId,
            role = MessageRole.ASSISTANT,
            content = content
        )
        val savedMessage = messageRepository.save(assistantMessage)
        
        // 채팅방 last_message_at 업데이트
        val chat = chatRepository.findById(chatId).get()
        chatRepository.save(
            chat.copy(
                lastMessageAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
        
        return savedMessage
    }
    
    @Transactional
    fun saveAiMessageWithSources(
        chatId: UUID, 
        content: String, 
        sources: List<MessageSource>,
        stoppedByUser: Boolean = false
    ): Message {
        val assistantMessage = Message(
            chatId = chatId,
            role = MessageRole.ASSISTANT,
            content = content,
            stoppedByUser = stoppedByUser
        )
        val savedMessage = messageRepository.save(assistantMessage)
        
        // 출처 정보 저장
        if (sources.isNotEmpty()) {
            val messageSources = sources.map { source ->
                source.copy(messageId = savedMessage.id)
            }
            messageSourceRepository.saveAll(messageSources)
        }
        
        // 채팅방 last_message_at 업데이트
        val chat = chatRepository.findById(chatId).get()
        chatRepository.save(
            chat.copy(
                lastMessageAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
        
        return savedMessage
    }
    
    @Transactional
    fun saveFirstChatResponse(
        chatId: UUID, 
        content: String, 
        logId: String,
        sources: List<MessageSource>,
        stoppedByUser: Boolean = false
    ): Message {
        // Chat에 logId 저장
        val chat = chatRepository.findById(chatId).get()
        chatRepository.save(
            chat.copy(
                logId = logId,
                lastMessageAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
        
        // AI 메시지 저장
        val assistantMessage = Message(
            chatId = chatId,
            role = MessageRole.ASSISTANT,
            content = content,
            stoppedByUser = stoppedByUser
        )
        val savedMessage = messageRepository.save(assistantMessage)
        
        // 출처 정보 저장
        if (sources.isNotEmpty()) {
            val messageSources = sources.map { source ->
                source.copy(messageId = savedMessage.id)
            }
            messageSourceRepository.saveAll(messageSources)
        }
        
        return savedMessage
    }
    
    fun isFirstMessage(chatId: UUID): Boolean {
        val messageCount = messageRepository.countByChatId(chatId)
        return messageCount == 0L
    }
    
    @Transactional
    fun saveUserMessage(chatId: UUID, content: String): Message {
        return messageRepository.save(
            Message(
                chatId = chatId,
                role = MessageRole.USER,
                content = content
            )
        )
    }
    
    fun getFirstMessage(chatId: UUID): String {
        return messageRepository.findByChatIdOrderByCreatedAtAsc(chatId)
            .firstOrNull()?.content
            ?: throw IllegalArgumentException("첫 메시지를 찾을 수 없습니다")
    }
    
    fun getConversationHistory(chatId: UUID): List<String> {
        val messages = messageRepository.findByChatIdOrderByCreatedAtAsc(chatId)
        return messages.dropLast(1).map { "${it.role}: ${it.content}" }
    }
    
    fun getLastMessage(chatId: UUID): String {
        return messageRepository.findByChatIdOrderByCreatedAtAsc(chatId)
            .lastOrNull()?.content ?: ""
    }
    
    fun getChat(chatId: UUID): Chat {
        return chatRepository.findById(chatId)
            .orElseThrow { IllegalArgumentException("채팅방을 찾을 수 없습니다: $chatId") }
    }
}