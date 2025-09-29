package com.aiva.user.grpc

import com.aiva.proto.user.*
import com.aiva.user.user.service.UserReadService
import io.grpc.Status
import io.grpc.StatusException
import mu.KotlinLogging
import net.devh.boot.grpc.server.service.GrpcService
import java.util.*

/**
 * User Service gRPC 서버 구현
 */
@GrpcService
class UserGrpcService(
    private val userReadService: UserReadService
) : UserServiceGrpcKt.UserServiceCoroutineImplBase() {
    
    private val logger = KotlinLogging.logger {}
    
    /**
     * 단일 사용자 정보 조회
     */
    override suspend fun getUserProfile(request: GetUserProfileRequest): GetUserProfileResponse {
        return try {
            logger.debug { "gRPC getUserProfile called for userId: ${request.userId}" }
            
            val userId = UUID.fromString(request.userId)
            val userResponse = userReadService.getUserInfo(userId.toString())
            
            val userProfile = UserProfile.newBuilder()
                .setUserId(userResponse.userId)
                .setNickname(userResponse.nickname)
                .apply {
                    userResponse.email?.let { setEmail(it) }
                    userResponse.avatarUrl?.let { setProfileImageUrl(it) }
                }
                .build()
            
            GetUserProfileResponse.newBuilder()
                .setUserProfile(userProfile)
                .build()
                
        } catch (e: IllegalArgumentException) {
            logger.warn { "User not found: ${request.userId}" }
            throw StatusException(Status.NOT_FOUND.withDescription("User not found: ${request.userId}"))
        } catch (e: Exception) {
            logger.error(e) { "Failed to get user profile for userId: ${request.userId}" }
            throw StatusException(Status.INTERNAL.withDescription("Internal server error"))
        }
    }
    
    /**
     * 여러 사용자 정보 배치 조회
     */
    override suspend fun getUserProfiles(request: GetUserProfilesRequest): GetUserProfilesResponse {
        return try {
            logger.debug { "gRPC getUserProfiles called for ${request.userIdsList.size} users" }
            
            val userProfiles = request.userIdsList.mapNotNull { userIdStr ->
                try {
                    val userId = UUID.fromString(userIdStr)
                    val userResponse = userReadService.getUserInfo(userId.toString())
                    
                    UserProfile.newBuilder()
                        .setUserId(userResponse.userId)
                        .setNickname(userResponse.nickname)
                        .apply {
                            userResponse.email?.let { setEmail(it) }
                            userResponse.avatarUrl?.let { setProfileImageUrl(it) }
                        }
                        .build()
                        
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to get user profile for userId: $userIdStr" }
                    null
                }
            }
            
            GetUserProfilesResponse.newBuilder()
                .addAllUserProfiles(userProfiles)
                .build()
                
        } catch (e: Exception) {
            logger.error(e) { "Failed to get user profiles" }
            throw StatusException(Status.INTERNAL.withDescription("Internal server error"))
        }
    }
}