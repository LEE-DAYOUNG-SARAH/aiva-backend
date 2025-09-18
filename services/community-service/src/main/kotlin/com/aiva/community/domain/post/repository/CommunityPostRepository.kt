package com.aiva.community.domain.post.repository

import com.aiva.community.domain.post.entity.CommunityPost
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
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
}