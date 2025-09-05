package com.aiva.community.post.repository

import com.aiva.community.post.entity.CommunityLike
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CommunityLikeRepository : JpaRepository<CommunityLike, UUID> {

    fun findByPostIdAndUserId(postId: UUID, userId: UUID): Optional<CommunityLike>

    @Query("SELECT COUNT(cl) FROM CommunityLike cl WHERE cl.postId = :postId")
    fun countByPostId(@Param("postId") postId: UUID): Long

    @Modifying
    @Query("DELETE FROM CommunityLike cl WHERE cl.postId = :postId AND cl.userId = :userId")
    fun deleteByPostIdAndUserId(@Param("postId") postId: UUID, @Param("userId") userId: UUID)

    @Query("SELECT EXISTS(SELECT 1 FROM CommunityLike cl WHERE cl.postId = :postId AND cl.userId = :userId)")
    fun existsByPostIdAndUserId(@Param("postId") postId: UUID, @Param("userId") userId: UUID): Boolean

    @Query("SELECT cl.userId FROM CommunityLike cl WHERE cl.postId = :postId")
    fun findUserIdsByPostId(@Param("postId") postId: UUID): List<UUID>
}