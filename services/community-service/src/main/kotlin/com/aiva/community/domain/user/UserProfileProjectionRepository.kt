package com.aiva.community.domain.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
interface UserProfileProjectionRepository : JpaRepository<UserProfileProjection, UUID> {
    
    /**
     * 다중 사용자 프로필 조회 (배치 조회 최적화)
     */
    @Query("SELECT u FROM UserProfileProjection u WHERE u.userId IN :userIds")
    fun findAllByUserIds(@Param("userIds") userIds: Collection<UUID>): List<UserProfileProjection>
    
    /**
     * 닉네임으로 검색
     */
    @Query("SELECT u FROM UserProfileProjection u WHERE u.nickname LIKE %:keyword%")
    fun findByNicknameContaining(@Param("keyword") keyword: String): List<UserProfileProjection>
    
    /**
     * 레벨별 조회
     */
    fun findByLevelGreaterThanEqual(level: Int): List<UserProfileProjection>
    
    /**
     * 최근 업데이트된 프로필 조회 (이벤트 순서 확인용)
     */
    @Query("SELECT u FROM UserProfileProjection u WHERE u.updatedAt >= :since ORDER BY u.updatedAt DESC")
    fun findRecentlyUpdated(@Param("since") since: Instant): List<UserProfileProjection>
    
    /**
     * MySQL UPSERT 구문 (ON DUPLICATE KEY UPDATE)
     * 이벤트 처리 시 버전 체크와 함께 upsert 수행
     */
    @Modifying
    @Query(value = """
        INSERT INTO user_profile_projection (user_id, nickname, avatar_url, level, version, updated_at)
        VALUES (UNHEX(REPLACE(:userId, '-', '')), :nickname, :avatarUrl, :level, :version, :updatedAt)
        ON DUPLICATE KEY UPDATE
            nickname = IF(version < VALUES(version), VALUES(nickname), nickname),
            avatar_url = IF(version < VALUES(version), VALUES(avatar_url), avatar_url),
            level = IF(version < VALUES(version), VALUES(level), level),
            updated_at = IF(version < VALUES(version), VALUES(updated_at), updated_at),
            version = GREATEST(version, VALUES(version))
    """, nativeQuery = true)
    fun upsertWithVersionCheck(
        @Param("userId") userId: String,
        @Param("nickname") nickname: String,
        @Param("avatarUrl") avatarUrl: String?,
        @Param("level") level: Int,
        @Param("version") version: Long,
        @Param("updatedAt") updatedAt: Instant
    ): Int
    
    /**
     * 특정 사용자의 현재 버전 조회 (순서 보장 체크용)
     */
    @Query("SELECT u.version FROM UserProfileProjection u WHERE u.userId = :userId")
    fun findVersionByUserId(@Param("userId") userId: UUID): Long?
}