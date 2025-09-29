package com.aiva.community.domain.user

import com.aiva.community.domain.post.dto.AuthorInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.util.*

/**
 * User Service와 REST API 통신을 담당하는 클라이언트
 * 
 * 사용자 프로필 정보를 실시간으로 조회합니다.
 */
@Service
class UserGrpcClient(
    @Value("\${user-service.url:http://localhost:8081}") private val userServiceUrl: String,
    private val objectMapper: ObjectMapper
) {
    
    private val logger = KotlinLogging.logger {}
    private val restClient = RestClient.builder()
        .baseUrl(userServiceUrl)
        .build()
    
    /**
     * 단일 사용자 정보 조회
     */
    fun getUserProfile(userId: UUID): AuthorInfo? {
        return try {
            logger.debug { "Fetching user profile for userId: $userId via REST API" }
            
            val response = restClient.get()
                .uri("/api/users/me")
                .header("X-User-Id", userId.toString())
                .retrieve()
                .onStatus({ status -> status.is4xxClientError || status.is5xxServerError }) { _, response ->
                    logger.warn { "User service returned error for userId: $userId, status: ${response.statusCode}" }
                }
                .body(String::class.java)
            
            response?.let { responseBody ->
                val apiResponse = objectMapper.readValue<Map<String, Any>>(responseBody)
                val userData = apiResponse["data"] as? Map<String, Any>
                
                userData?.let {
                    AuthorInfo(
                        userId = userId,
                        nickname = it["nickname"]?.toString() ?: "Unknown User",
                        profileImageUrl = it["avatarUrl"]?.toString()
                    )
                }
            }
            
        } catch (e: RestClientException) {
            logger.error(e) { "REST client error while fetching user profile for userId: $userId" }
            null
        } catch (e: Exception) {
            logger.error(e) { "Failed to fetch user profile for userId: $userId" }
            null
        }
    }
    
    /**
     * 여러 사용자 정보 배치 조회
     * 현재는 개별 호출로 구현, 향후 배치 API 추가 시 최적화 가능
     */
    fun getUserProfiles(userIds: Collection<UUID>): Map<UUID, AuthorInfo> {
        if (userIds.isEmpty()) return emptyMap()
        
        logger.debug { "Fetching ${userIds.size} user profiles via REST API batch calls" }
        
        return userIds.mapNotNull { userId ->
            getUserProfile(userId)?.let { profile ->
                userId to profile
            }
        }.toMap()
    }
    
    /**
     * 사용자 정보 조회 실패 시 폴백 정보 생성
     */
    fun createFallbackAuthorInfo(userId: UUID): AuthorInfo {
        logger.warn { "Creating fallback author info for userId: $userId" }
        return AuthorInfo(
            userId = userId,
            nickname = "Unknown User",
            profileImageUrl = null
        )
    }
}