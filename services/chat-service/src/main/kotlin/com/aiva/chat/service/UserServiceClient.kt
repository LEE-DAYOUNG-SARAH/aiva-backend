package com.aiva.chat.service

import com.aiva.chat.dto.user.UserInfo
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.*

@Service
class UserServiceClient(
    private val webClient: WebClient
) {
    
    @Value("\${user-service.base-url:http://localhost:8081}")
    private lateinit var userServiceBaseUrl: String
    
    fun getUserInfo(userId: UUID): Mono<UserInfo> {
        return webClient
            .get()
            .uri("$userServiceBaseUrl/api/users/$userId/info")
            .retrieve()
            .bodyToMono(UserInfo::class.java)
            .onErrorMap { error ->
                RuntimeException("사용자 정보 조회 실패: ${error.message}", error)
            }
    }
}