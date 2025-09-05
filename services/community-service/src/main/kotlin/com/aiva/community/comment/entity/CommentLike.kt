package com.aiva.community.comment.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "comment_likes")
@EntityListeners(AuditingEntityListener::class)
data class CommentLike(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "comment_id", nullable = false)
    val commentId: UUID,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @Table(
        uniqueConstraints = [
            UniqueConstraint(columnNames = ["comment_id", "user_id"])
        ]
    )
    companion object
}