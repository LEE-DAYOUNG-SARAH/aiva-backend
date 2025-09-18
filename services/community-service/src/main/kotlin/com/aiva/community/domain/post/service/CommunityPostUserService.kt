package com.aiva.community.domain.post.service

import com.aiva.community.domain.user.AuthorInfo
import com.aiva.community.domain.user.UserProfileProjectionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 커뮤니티 게시물과 관련된 사용자 정보 처리 서비스
 * 
 * 로컬 프로젝션을 통해 외부 서비스 호출 없이 사용자 정보를 제공한다.
 * 캐시 미스 시에도 로컬 DB만 조회하여 안정적인 응답을 보장한다.
 */
@Service
@Transactional(readOnly = true)
class CommunityPostUserService(
    private val userProfileRepository: UserProfileProjectionRepository
) {
    
    private val logger = LoggerFactory.getLogger(CommunityPostUserService::class.java)
    
    /**
     * 단일 사용자 프로필 조회
     */
    fun getUserProfile(userId: UUID): AuthorInfo? {
        return try {
            userProfileRepository.findById(userId)
                .map { it.toAuthorInfo() }
                .orElse(null)
        } catch (e: Exception) {
            logger.error("Failed to get user profile for userId: $userId", e)
            null
        }
    }
    
    /**
     * 다중 사용자 프로필 배치 조회 (N+1 문제 해결)
     * 
     * 커뮤니티 목록 조회 시 사용자 정보를 한 번에 가져온다.
     * 로컬 프로젝션이므로 외부 서비스 호출 없이 빠른 응답 가능
     */
    fun getUserProfiles(userIds: Collection<UUID>): Map<UUID, AuthorInfo> {
        if (userIds.isEmpty()) return emptyMap()
        
        return try {
            val profiles = userProfileRepository.findAllByUserIds(userIds)
            logger.debug("Retrieved {} user profiles from local projection", profiles.size)
            
            profiles.associate { it.userId to it.toAuthorInfo() }
        } catch (e: Exception) {
            logger.error("Failed to get user profiles for userIds: $userIds", e)
            emptyMap()
        }
    }
    
    /**
     * 기본 사용자 정보 생성 (프로젝션에 없는 경우 fallback)
     */
    fun createFallbackAuthorInfo(userId: UUID): AuthorInfo {
        return AuthorInfo(
            userId = userId,
            nickname = "Unknown User",
            profileImageUrl = null
        )
    }
    
    /**
     * 사용자 프로필 존재 여부 확인
     */
    fun existsUserProfile(userId: UUID): Boolean {
        return try {
            userProfileRepository.existsById(userId)
        } catch (e: Exception) {
            logger.error("Failed to check user profile existence for userId: $userId", e)
            false
        }
    }
    
    /**
     * 닉네임 검색
     */
    fun searchUsersByNickname(keyword: String): List<AuthorInfo> {
        return try {
            userProfileRepository.findByNicknameContaining(keyword)
                .map { it.toAuthorInfo() }
        } catch (e: Exception) {
            logger.error("Failed to search users by nickname: $keyword", e)
            emptyList()
        }
    }
    
    /**
     * 통계용: 전체 사용자 수 (프로젝션 기준)
     */
    fun getTotalUserCount(): Long {
        return try {
            userProfileRepository.count()
        } catch (e: Exception) {
            logger.error("Failed to get total user count", e)
            0L
        }
    }
}