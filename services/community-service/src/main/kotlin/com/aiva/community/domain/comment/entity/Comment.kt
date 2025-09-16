package com.aiva.community.domain.comment.entity

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
    var content: String,
    
    @Column(name = "like_count", nullable = false)
    var likeCount: Int = 0,
    
    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null,
    
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

    fun update(content: String) { this.content = content }

    fun delete() { this.deletedAt = LocalDateTime.now() }
}
