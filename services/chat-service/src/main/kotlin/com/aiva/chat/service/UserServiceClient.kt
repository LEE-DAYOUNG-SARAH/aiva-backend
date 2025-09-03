package com.aiva.chat.service

import com.aiva.common.dto.ChildData
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.*

@Service
class UserServiceClient(
    @Value("\${user-service.base-url:http://localhost:8081}")
    private val userServiceBaseUrl: String,
    private val webClient: WebClient,
) {
    fun getChildData(userId: UUID): Mono<ChildData> {
        return webClient
            .get()
            .uri("$userServiceBaseUrl/api/children/$userId")
            .retrieve()
            .bodyToMono(ChildData::class.java)
            .onErrorMap { error ->
                RuntimeException("사용자 정보 조회 실패: ${error.message}", error)
            }
    }
}