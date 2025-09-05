package com.aiva.common.client

import com.aiva.common.dto.ChildData
import com.aiva.common.dto.UserData
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*

@Component
class UserServiceClient(
    @Qualifier(WebClientConfig.USER_SERVICE_WEB_CLIENT)
    private val webClient: WebClient
) {

    fun getUserData(userId: UUID): UserData? {
        return try {
            webClient.get()
                .uri("/api/v1/users/{userId}", userId)
                .retrieve()
                .bodyToMono(UserData::class.java)
                .timeout(Duration.ofSeconds(3))
                .block()
        } catch (e: Exception) {
            null
        }
    }

    fun getAllUserData(userIds: List<UUID>): List<UserData> {
        return try {
            webClient.post()
                .uri("/api/v1/users/batch")
                .bodyValue(mapOf("userIds" to userIds))
                .retrieve()
                .bodyToMono<List<UserData>>()
                .timeout(Duration.ofSeconds(5))
                .block() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getChildData(userId: UUID): ChildData? {
        return try {
            webClient.get()
                .uri("/api/children/{userId}", userId)
                .retrieve()
                .bodyToMono(ChildData::class.java)
                .timeout(Duration.ofSeconds(3))
                .block()
        } catch (e: Exception) {
            null
        }
    }
}