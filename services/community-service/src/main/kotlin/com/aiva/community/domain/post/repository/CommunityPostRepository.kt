package com.aiva.community.domain.post.repository

import com.aiva.community.domain.post.entity.CommunityPost
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface CommunityPostRepository : JpaRepository<CommunityPost, UUID> {

    @Query("SELECT p FROM CommunityPost p WHERE p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    fun findActivePosts(pageable: Pageable): Page<CommunityPost>

    @Query("SELECT p FROM CommunityPost p WHERE p.userId = :userId AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    fun findActivePostsByUserId(@Param("userId") userId: UUID, pageable: Pageable): Page<CommunityPost>

    @Query("SELECT p FROM CommunityPost p WHERE p.deletedAt IS NULL ORDER BY p.likeCount DESC, p.commentCount DESC, p.createdAt DESC")
    fun findPopularPosts(pageable: Pageable): Page<CommunityPost>

    @Query("SELECT p FROM CommunityPost p WHERE p.id = :postId AND p.deletedAt IS NULL")
    fun findActivePostById(@Param("postId") postId: UUID): Optional<CommunityPost>

    @Query("SELECT COUNT(p) FROM CommunityPost p WHERE p.userId = :userId AND p.deletedAt IS NULL")
    fun countActivePostsByUserId(@Param("userId") userId: UUID): Long
    
    @Query("SELECT COUNT(p) FROM CommunityPost p WHERE p.deletedAt IS NULL")
    fun countActivePosts(): Long
    
    @Query("SELECT p FROM CommunityPost p WHERE p.id IN :postIds AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    fun findActivePostsByIds(@Param("postIds") postIds: List<UUID>): List<CommunityPost>
    
    /**
     * 커서 기반 페이지네이션을 위한 Keyset 쿼리
     */
    @Query("""
        SELECT p FROM CommunityPost p 
        WHERE p.deletedAt IS NULL 
        AND ((p.createdAt < :lastCreatedAt) 
             OR (p.createdAt = :lastCreatedAt AND p.id < :lastPostId))
        ORDER BY p.createdAt DESC, p.id DESC
    """)
    fun findActivePostsWithKeyset(
        @Param("lastCreatedAt") lastCreatedAt: LocalDateTime,
        @Param("lastPostId") lastPostId: UUID,
        pageable: Pageable
    ): List<CommunityPost>
    
    // 오버로드 메서드 (limit만 받는 버전)
    @Query("""
        SELECT p FROM CommunityPost p 
        WHERE p.deletedAt IS NULL 
        AND ((p.createdAt < :lastCreatedAt) 
             OR (p.createdAt = :lastCreatedAt AND p.id < :lastPostId))
        ORDER BY p.createdAt DESC, p.id DESC
        LIMIT :limit
    """)
    fun findActivePostsWithKeyset(
        @Param("lastCreatedAt") lastCreatedAt: LocalDateTime,
        @Param("lastPostId") lastPostId: UUID,
        @Param("limit") limit: Int
    ): List<CommunityPost>
    
    /**
     * 특정 시간 이후의 게시물 조회 (24시간 cutoff용)
     */
    @Query("""
        SELECT p FROM CommunityPost p 
        WHERE p.deletedAt IS NULL 
        AND p.createdAt <= :fromTime
        ORDER BY p.createdAt DESC, p.id DESC
        LIMIT :limit
    """)
    fun findActivePostsFromTime(
        @Param("fromTime") fromTime: LocalDateTime,
        @Param("limit") limit: Int
    ): List<CommunityPost>
}