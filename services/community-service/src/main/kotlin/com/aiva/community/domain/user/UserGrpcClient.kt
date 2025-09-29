package com.aiva.community.domain.user

import com.aiva.community.domain.post.dto.AuthorInfo
import com.aiva.proto.user.*
import com.aiva.common.redis.service.RedisCommunityServiceV2
import io.grpc.StatusException
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Service
import java.util.*

/**
 * User Service와 gRPC 통신을 담당하는 클라이언트
 * 
 * 캐시 우선 조회 → gRPC 호출 순서로 사용자 프로필 정보를 조회합니다.
 */
@Service
class UserGrpcClient(
    private val redisCommunityServiceV2: RedisCommunityServiceV2
) {
    
    private val logger = KotlinLogging.logger {}
    
    @GrpcClient("user-service")
    private lateinit var userServiceStub: UserServiceGrpcKt.UserServiceCoroutineStub
    
    /**
     * 단일 사용자 정보 조회 (캐시 우선)
     */
    fun getUserProfile(userId: UUID): AuthorInfo? {
        // 1. 캐시에서 우선 조회
        val cachedProfile = redisCommunityServiceV2.getUserProfile(userId)
        if (cachedProfile != null) {
            logger.debug { "Cache hit for user profile: $userId" }
            return AuthorInfo(
                userId = userId,
                nickname = cachedProfile["nickname"]?.toString() ?: "Unknown",
                profileImageUrl = cachedProfile["profileUrl"]?.toString()
            )
        }
        
        // 2. 캐시 미스 시 gRPC 호출
        return try {
            logger.debug { "Cache miss, fetching user profile for userId: $userId via gRPC" }
            
            val request = GetUserProfileRequest.newBuilder()
                .setUserId(userId.toString())
                .build()
            
            val response = runBlocking {
                userServiceStub.getUserProfile(request)
            }
            
            val userProfile = response.userProfile
            val authorInfo = AuthorInfo(
                userId = UUID.fromString(userProfile.userId),
                nickname = userProfile.nickname,
                profileImageUrl = if (userProfile.hasProfileImageUrl()) userProfile.profileImageUrl else null
            )
            
            // 3. 조회한 결과를 캐시에 저장
            redisCommunityServiceV2.cacheUserProfile(userId, userProfile.nickname, 
                if (userProfile.hasProfileImageUrl()) userProfile.profileImageUrl else null)
            
            authorInfo
            
        } catch (e: StatusException) {
            logger.warn { "gRPC error while fetching user profile for userId: $userId, status: ${e.status}" }
            null
        } catch (e: Exception) {
            logger.error(e) { "Failed to fetch user profile for userId: $userId" }
            null
        }
    }
    
    /**
     * 여러 사용자 정보 배치 조회 (캐시 우선)
     */
    fun getUserProfiles(userIds: Collection<UUID>): Map<UUID, AuthorInfo> {
        if (userIds.isEmpty()) return emptyMap()
        
        val result = mutableMapOf<UUID, AuthorInfo>()
        
        // 1. 캐시에서 배치 조회
        val cachedProfiles = redisCommunityServiceV2.getUserProfiles(userIds)
        cachedProfiles.forEach { (userId, profile) ->
            result[userId] = AuthorInfo(
                userId = userId,
                nickname = profile["nickname"]?.toString() ?: "Unknown",
                profileImageUrl = profile["profileUrl"]?.toString()
            )
        }
        
        // 2. 캐시 미스된 사용자들만 gRPC로 조회
        val cacheMissUserIds = userIds - cachedProfiles.keys
        if (cacheMissUserIds.isNotEmpty()) {
            try {
                logger.debug { "Cache miss for ${cacheMissUserIds.size} users, fetching via gRPC batch call" }
                
                val request = GetUserProfilesRequest.newBuilder()
                    .addAllUserIds(cacheMissUserIds.map { it.toString() })
                    .build()
                
                val response = runBlocking {
                    userServiceStub.getUserProfiles(request)
                }
                
                response.userProfilesList.forEach { userProfile ->
                    val userId = UUID.fromString(userProfile.userId)
                    val authorInfo = AuthorInfo(
                        userId = userId,
                        nickname = userProfile.nickname,
                        profileImageUrl = if (userProfile.hasProfileImageUrl()) userProfile.profileImageUrl else null
                    )
                    
                    result[userId] = authorInfo
                    
                    // 3. 조회한 결과를 캐시에 저장
                    redisCommunityServiceV2.cacheUserProfile(userId, userProfile.nickname,
                        if (userProfile.hasProfileImageUrl()) userProfile.profileImageUrl else null)
                }
                
            } catch (e: StatusException) {
                logger.warn { "gRPC error while fetching user profiles, status: ${e.status}" }
            } catch (e: Exception) {
                logger.error(e) { "Failed to fetch user profiles for userIds: $cacheMissUserIds" }
            }
        }
        
        logger.debug { "Retrieved ${result.size} user profiles (${cachedProfiles.size} from cache, ${result.size - cachedProfiles.size} from gRPC)" }
        return result
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