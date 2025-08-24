package com.aiva.batch.repository.user

import com.aiva.batch.entity.user.BatchUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

/**
 * 배치 처리용 User Repository (읽기 전용)
 */
@Repository
interface BatchUserRepository : JpaRepository<BatchUser, UUID> {
    
    /**
     * Pro 권한이 만료된 사용자들 조회
     */
    @Query("""
        SELECT u FROM BatchUser u 
        WHERE u.isPro = true 
        AND u.proExpiresAt <= :currentTime
        AND u.deletedAt IS NULL
    """)
    fun findExpiredProUsers(@Param("currentTime") currentTime: LocalDateTime): List<BatchUser>
    
    /**
     * 비활성 사용자들 조회 (1년 이상 로그인하지 않은 사용자)
     */
    @Query("""
        SELECT u FROM BatchUser u 
        WHERE u.lastLoginAt < :oneYearAgo
        AND u.deletedAt IS NULL
    """)
    fun findInactiveUsers(@Param("oneYearAgo") oneYearAgo: LocalDateTime): List<BatchUser>
    
    /**
     * 특정 사용자 ID 목록으로 사용자 조회
     */
    @Query("""
        SELECT u FROM BatchUser u 
        WHERE u.id IN :userIds
        AND u.deletedAt IS NULL
    """)
    fun findByUserIds(@Param("userIds") userIds: List<UUID>): List<BatchUser>
}