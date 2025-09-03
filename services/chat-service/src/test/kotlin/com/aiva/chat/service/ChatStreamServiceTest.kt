package com.aiva.chat.service

import com.aiva.chat.dto.AiChatRequest
import com.aiva.common.dto.ChildData
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.time.Duration
import java.time.LocalDate
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class ChatStreamServiceTest {

    @Mock
    private lateinit var chatManagementService: ChatManagementService
    
    @Mock
    private lateinit var aiService: AiService
    
    @Mock
    private lateinit var aiResponseParser: AiResponseParser

    private lateinit var chatStreamService: ChatStreamService

    @BeforeEach
    fun setUp() {
        chatStreamService = ChatStreamService(chatManagementService, aiService, aiResponseParser)
    }

    @Test
    fun `스트림 응답 기본 플로우 테스트`() {
        val chatId = UUID.randomUUID()
        val sessionId = "test-session"
        val request = createTestAiRequest()
        
        val mockStreamResponse = Flux.just(
            "event: start\n\n",
            "data: {\"delta\": \"안녕\"}\n\n",
            "data: {\"delta\": \"하세요\"}\n\n",
            "event: done\n\n"
        )

        given(aiService.streamChatResponse(request))
            .willReturn(mockStreamResponse)
        given(aiResponseParser.parseEventLine(anyString()))
            .willReturn(null) // 단순 테스트를 위해 null 반환

        val resultFlux = chatStreamService.streamChatResponse(request, chatId, sessionId, false)

        StepVerifier.create(resultFlux)
            .expectNext("event: start\n\n")
            .expectNext("data: {\"delta\": \"안녕\"}\n\n")
            .expectNext("data: {\"delta\": \"하세요\"}\n\n")
            .expectNext("event: done\n\n")
            .verifyComplete()
    }

    @Test
    fun `다중 세션 독립적 스트리밍 테스트`() {
        val chatId = UUID.randomUUID()
        val session1 = "device1"
        val session2 = "device2"
        val request = createTestAiRequest()

        val mockStream1 = Flux.just("data: {\"delta\": \"Device1\"}\n\n")
            .delayElements(Duration.ofMillis(100))
        val mockStream2 = Flux.just("data: {\"delta\": \"Device2\"}\n\n")
            .delayElements(Duration.ofMillis(100))

        given(aiService.streamChatResponse(request))
            .willReturn(mockStream1)
            .willReturn(mockStream2)

        // 두 세션 동시 시작
        val stream1 = chatStreamService.streamChatResponse(request, chatId, session1, false)
        val stream2 = chatStreamService.streamChatResponse(request, chatId, session2, false)

        // 각 스트림이 독립적으로 동작하는지 검증
        StepVerifier.create(stream1.take(1))
            .expectNext("data: {\"delta\": \"Device1\"}\n\n")
            .verifyComplete()

        StepVerifier.create(stream2.take(1))
            .expectNext("data: {\"delta\": \"Device2\"}\n\n")
            .verifyComplete()
    }

    @Test
    fun `세션별 독립적 취소 테스트`() {
        val chatId = UUID.randomUUID()
        val session1 = "device1"
        val session2 = "device2"
        val request = createTestAiRequest()

        // 무한 스트림 시뮬레이션
        val infiniteStream = Flux.interval(Duration.ofMillis(100))
            .map { "data: {\"delta\": \"chunk-$it\"}\n\n" }

        given(aiService.streamChatResponse(request))
            .willReturn(infiniteStream)

        // 두 세션 시작
        val stream1 = chatStreamService.streamChatResponse(request, chatId, session1, false)
        val stream2 = chatStreamService.streamChatResponse(request, chatId, session2, false)

        // Session1만 취소
        val cancelResult1 = chatStreamService.cancelChatStream(chatId, session1)
        assertTrue(cancelResult1, "Session1 취소가 성공해야 함")

        // Session1 스트림이 취소되었는지 확인
        StepVerifier.create(stream1.take(Duration.ofSeconds(1)))
            .expectNextCount(0) // 취소되어 데이터 없음
            .verifyComplete()

        // Session2는 여전히 동작하는지 확인
        val cancelResult2 = chatStreamService.cancelChatStream(chatId, session2)
        assertTrue(cancelResult2, "Session2도 취소 가능해야 함")
    }

    @Test
    fun `존재하지 않는 세션 취소 테스트`() {
        val chatId = UUID.randomUUID()
        val nonExistentSession = "non-existent"

        val result = chatStreamService.cancelChatStream(chatId, nonExistentSession)

        assertFalse(result, "존재하지 않는 세션은 취소 실패해야 함")
    }

    @Test
    fun `스트림 취소 후 재시작 테스트`() {
        val chatId = UUID.randomUUID()
        val sessionId = "restart-test"
        val request = createTestAiRequest()

        given(aiService.streamChatResponse(request))
            .willReturn(Flux.just("data: {\"delta\": \"첫번째\"}\n\n"))
            .willReturn(Flux.just("data: {\"delta\": \"두번째\"}\n\n"))

        // 첫 번째 스트림 시작
        val stream1 = chatStreamService.streamChatResponse(request, chatId, sessionId, false)
        
        // 취소
        chatStreamService.cancelChatStream(chatId, sessionId)
        
        // 같은 세션으로 새로운 스트림 시작 (새로운 요청)
        val stream2 = chatStreamService.streamChatResponse(request, chatId, sessionId, false)

        StepVerifier.create(stream2.take(1))
            .expectNext("data: {\"delta\": \"두번째\"}\n\n")
            .verifyComplete()
    }

    @Test
    fun `스트림 에러 처리 테스트`() {
        val chatId = UUID.randomUUID()
        val sessionId = "error-test"
        val request = createTestAiRequest()

        val errorStream = Flux.error<String>(RuntimeException("AI 서버 에러"))

        given(aiService.streamChatResponse(request))
            .willReturn(errorStream)

        val resultStream = chatStreamService.streamChatResponse(request, chatId, sessionId, false)

        StepVerifier.create(resultStream)
            .verifyError(RuntimeException::class.java)
    }

    private fun createTestAiRequest(): AiChatRequest {
        val userId = UUID.randomUUID()
        val childData = ChildData(
            childId = UUID.randomUUID(),
            isBorn = true,
            childBirthdate = LocalDate.of(2020, 1, 1),
            gender = "FEMALE"
        )
        return AiChatRequest.fromNewChat(userId, "테스트 질문", childData)
    }
}