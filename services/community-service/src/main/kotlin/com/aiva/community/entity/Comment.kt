package com.aiva.community.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "comments")
@EntityListeners(AuditingEntityListener::class)
data class Comment(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "post_id", nullable = false)
    val postId: UUID,
    
    @Column(name = "user_id", nullable = false)
    val userId: UUID,
    
    @Column(name = "parent_comment_id")
    val parentCommentId: UUID? = null,
    
    @Column(name = "content", nullable = false, length = 300)
    val content: String,
    
    @Column(name = "like_count", nullable = false)
    var likeCount: Int = 0,
    
    @Column(name = "deleted_at")
    val deletedAt: LocalDateTime? = null,
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @OneToMany(mappedBy = "commentId", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val likes: List<CommentLike> = mutableListOf()
) {
    fun incrementLikeCount() { likeCount++ }
    fun decrementLikeCount() { if (likeCount > 0) likeCount-- }
}

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

@Entity
@Table(name = "reports")
@EntityListeners(AuditingEntityListener::class)
data class Report(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "reporter_user_id", nullable = false)
    val reporterUserId: UUID,
    
    @Column(name = "target_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val targetType: ReportTargetType,
    
    @Column(name = "target_id", nullable = false)
    val targetId: UUID,
    
    @Column(name = "reason_code", nullable = false, length = 30)
    val reasonCode: String,
    
    @Column(name = "details", columnDefinition = "TEXT")
    val details: String? = null,
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class ReportTargetType {
    POST, COMMENT
}
