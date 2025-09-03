package com.aiva.chat.service

import com.aiva.chat.entity.Chat
import com.aiva.chat.entity.Message
import com.aiva.chat.entity.MessageRole
import com.aiva.chat.entity.MessageSource
import com.aiva.chat.repository.ChatRepository
import com.aiva.chat.repository.MessageRepository
import com.aiva.chat.repository.MessageSourceRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.*
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class ChatManagementServiceTest {

    @Mock
    private lateinit var chatRepository: ChatRepository
    
    @Mock
    private lateinit var messageRepository: MessageRepository
    
    @Mock
    private lateinit var messageSourceRepository: MessageSourceRepository

    private lateinit var chatManagementService: ChatManagementService

    @BeforeEach
    fun setUp() {
        chatManagementService = ChatManagementService(
            chatRepository, 
            messageRepository, 
            messageSourceRepository
        )
    }

    @Test
    fun `새 채팅 생성 테스트`() {
        val userId = UUID.randomUUID()
        val chatId = UUID.randomUUID()
        val chat = Chat(id = chatId, userId = userId)

        given(chatRepository.save(any<Chat>()))
            .willReturn(chat)

        val result = chatManagementService.createNewChat(userId)

        assertEquals(chatId, result.chatId)
        verify(chatRepository).save(any<Chat>())
    }

    @Test
    fun `첫 메시지 확인 테스트`() {
        val chatId = UUID.randomUUID()

        // 첫 메시지인 경우
        given(messageRepository.countByChatId(chatId))
            .willReturn(0L)
        assertTrue(chatManagementService.isFirstMessage(chatId))

        // 기존 메시지가 있는 경우
        given(messageRepository.countByChatId(chatId))
            .willReturn(3L)
        assertFalse(chatManagementService.isFirstMessage(chatId))
    }

    @Test
    fun `사용자 메시지 저장 테스트`() {
        val chatId = UUID.randomUUID()
        val content = "안녕하세요"
        val savedMessage = Message(
            chatId = chatId,
            role = MessageRole.USER,
            content = content
        )

        given(messageRepository.save(any<Message>()))
            .willReturn(savedMessage)

        val result = chatManagementService.saveUserMessage(chatId, content)

        assertEquals(chatId, result.chatId)
        assertEquals(MessageRole.USER, result.role)
        assertEquals(content, result.content)
        verify(messageRepository).save(any<Message>())
    }

    @Test
    fun `AI 메시지 출처와 함께 저장 테스트`() {
        val chatId = UUID.randomUUID()
        val content = "AI 응답입니다"
        val sources = listOf(
            MessageSource(
                messageId = UUID.randomUUID(),
                title = "육아 가이드",
                link = "https://example.com/guide"
            )
        )
        val chat = Chat(id = chatId, userId = UUID.randomUUID())
        val savedMessage = Message(
            chatId = chatId,
            role = MessageRole.ASSISTANT,
            content = content,
            stoppedByUser = false
        )

        given(messageRepository.save(any<Message>()))
            .willReturn(savedMessage)
        given(chatRepository.findById(chatId))
            .willReturn(Optional.of(chat))
        given(chatRepository.save(any<Chat>()))
            .willReturn(chat.copy(lastMessageAt = LocalDateTime.now()))
        given(messageSourceRepository.saveAll(anyList<MessageSource>()))
            .willReturn(sources)

        val result = chatManagementService.saveAiMessageWithSources(
            chatId = chatId,
            content = content,
            sources = sources,
            stoppedByUser = false
        )

        assertEquals(content, result.content)
        assertEquals(MessageRole.ASSISTANT, result.role)
        assertFalse(result.stoppedByUser)
        
        verify(messageRepository).save(any<Message>())
        verify(messageSourceRepository).saveAll(anyList<MessageSource>())
        verify(chatRepository).save(any<Chat>())
    }

    @Test
    fun `사용자 취소된 AI 메시지 저장 테스트`() {
        val chatId = UUID.randomUUID()
        val content = "부분적 AI 응답"
        val chat = Chat(id = chatId, userId = UUID.randomUUID())
        val savedMessage = Message(
            chatId = chatId,
            role = MessageRole.ASSISTANT,
            content = content,
            stoppedByUser = true
        )

        given(messageRepository.save(any<Message>()))
            .willReturn(savedMessage)
        given(chatRepository.findById(chatId))
            .willReturn(Optional.of(chat))
        given(chatRepository.save(any<Chat>()))
            .willReturn(chat.copy(lastMessageAt = LocalDateTime.now()))

        val result = chatManagementService.saveAiMessageWithSources(
            chatId = chatId,
            content = content,
            sources = emptyList(),
            stoppedByUser = true
        )

        assertTrue(result.stoppedByUser, "사용자 취소 플래그가 설정되어야 함")
        verify(messageSourceRepository, never()).saveAll(anyList<MessageSource>())
    }

    @Test
    fun `첫 채팅 응답 저장 테스트`() {
        val chatId = UUID.randomUUID()
        val content = "첫 AI 응답"
        val logId = "ai-log-123"
        val sources = listOf(
            MessageSource(
                messageId = UUID.randomUUID(),
                title = "첫 출처",
                link = "https://example.com/first"
            )
        )
        val chat = Chat(id = chatId, userId = UUID.randomUUID())
        val savedMessage = Message(
            chatId = chatId,
            role = MessageRole.ASSISTANT,
            content = content
        )

        given(chatRepository.findById(chatId))
            .willReturn(Optional.of(chat))
        given(chatRepository.save(any<Chat>()))
            .willReturn(chat.copy(logId = logId))
        given(messageRepository.save(any<Message>()))
            .willReturn(savedMessage)
        given(messageSourceRepository.saveAll(anyList<MessageSource>()))
            .willReturn(sources)

        val result = chatManagementService.saveFirstChatResponse(
            chatId = chatId,
            content = content,
            logId = logId,
            sources = sources,
            stoppedByUser = false
        )

        assertEquals(content, result.content)
        verify(chatRepository).save(argThat<Chat> { chat -> 
            chat.logId == logId 
        })
        verify(messageRepository).save(any<Message>())
        verify(messageSourceRepository).saveAll(anyList<MessageSource>())
    }

    @Test
    fun `채팅방 조회 테스트`() {
        val chatId = UUID.randomUUID()
        val chat = Chat(id = chatId, userId = UUID.randomUUID())

        given(chatRepository.findById(chatId))
            .willReturn(Optional.of(chat))

        val result = chatManagementService.getChat(chatId)

        assertEquals(chatId, result.id)
        verify(chatRepository).findById(chatId)
    }

    @Test
    fun `존재하지 않는 채팅방 조회 실패 테스트`() {
        val chatId = UUID.randomUUID()

        given(chatRepository.findById(chatId))
            .willReturn(Optional.empty())

        try {
            chatManagementService.getChat(chatId)
            throw AssertionError("예외가 발생해야 함")
        } catch (e: IllegalArgumentException) {
            kotlin.test.assertTrue(e.message!!.contains("채팅방을 찾을 수 없습니다"))
        }
    }

    @Test
    fun `대화 히스토리 조회 테스트`() {
        val chatId = UUID.randomUUID()
        val messages = listOf(
            Message(
                chatId = chatId,
                role = MessageRole.USER,
                content = "사용자 질문"
            ),
            Message(
                chatId = chatId,
                role = MessageRole.ASSISTANT,
                content = "AI 응답"
            ),
            Message(
                chatId = chatId,
                role = MessageRole.USER,
                content = "최신 메시지"
            )
        )

        given(messageRepository.findByChatIdOrderByCreatedAtAsc(chatId))
            .willReturn(messages)

        val history = chatManagementService.getConversationHistory(chatId)

        assertEquals(2, history.size) // 마지막 메시지 제외
        assertEquals("USER: 사용자 질문", history[0])
        assertEquals("ASSISTANT: AI 응답", history[1])
    }
}