package com.aiva.community.domain.comment.repository

import com.aiva.community.domain.comment.entity.Comment
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CommentRepository : JpaRepository<Comment, UUID> {

    @Query("SELECT c FROM Comment c WHERE c.postId = :postId AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    fun findActiveCommentsByPostId(@Param("postId") postId: UUID, pageable: Pageable): Page<Comment>

    @Query("SELECT c FROM Comment c WHERE c.postId = :postId AND c.parentCommentId IS NULL AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
    fun findTopLevelCommentsByPostId(@Param("postId") postId: UUID, pageable: Pageable): Page<Comment>

    @Query("SELECT c FROM Comment c WHERE c.parentCommentId = :parentCommentId AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
    fun findRepliesByParentCommentId(@Param("parentCommentId") parentCommentId: UUID): List<Comment>

    @Query("SELECT c FROM Comment c WHERE c.id = :commentId AND c.deletedAt IS NULL")
    fun findActiveCommentById(@Param("commentId") commentId: UUID): Optional<Comment>

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.postId = :postId AND c.deletedAt IS NULL")
    fun countActiveCommentsByPostId(@Param("postId") postId: UUID): Long

    @Query("SELECT c FROM Comment c WHERE c.userId = :userId AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    fun findActiveCommentsByUserId(@Param("userId") userId: UUID, pageable: Pageable): Page<Comment>
}