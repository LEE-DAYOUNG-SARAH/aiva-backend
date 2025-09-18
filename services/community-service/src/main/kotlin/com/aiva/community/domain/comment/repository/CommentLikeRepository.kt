package com.aiva.community.domain.comment.repository

import com.aiva.community.domain.comment.entity.CommentLike
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CommentLikeRepository : JpaRepository<CommentLike, UUID> {

    fun findByCommentIdAndUserId(commentId: UUID, userId: UUID): Optional<CommentLike>

    @Query("SELECT COUNT(cl) FROM CommentLike cl WHERE cl.commentId = :commentId")
    fun countByCommentId(@Param("commentId") commentId: UUID): Long

    @Modifying
    @Query("DELETE FROM CommentLike cl WHERE cl.commentId = :commentId AND cl.userId = :userId")
    fun deleteByCommentIdAndUserId(@Param("commentId") commentId: UUID, @Param("userId") userId: UUID)

    @Query("SELECT EXISTS(SELECT 1 FROM CommentLike cl WHERE cl.commentId = :commentId AND cl.userId = :userId)")
    fun existsByCommentIdAndUserId(@Param("commentId") commentId: UUID, @Param("userId") userId: UUID): Boolean

    @Query("SELECT cl.userId FROM CommentLike cl WHERE cl.commentId = :commentId")
    fun findUserIdsByCommentId(@Param("commentId") commentId: UUID): List<UUID>
}