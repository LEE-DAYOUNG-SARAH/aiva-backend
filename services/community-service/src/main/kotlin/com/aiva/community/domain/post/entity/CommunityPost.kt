package com.aiva.community.domain.post.entity

import com.aiva.community.domain.comment.entity.Comment
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "community_posts")
@EntityListeners(AuditingEntityListener::class)
data class CommunityPost(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "title", nullable = false, length = 50)
    var title: String,
    
    @Column(name = "content", nullable = false, length = 1000)
    var content: String,
    
    @Column(name = "like_count", nullable = false)
    var likeCount: Int = 0,
    
    @Column(name = "comment_count", nullable = false)
    var commentCount: Int = 0,
    
    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null,
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @OneToMany(mappedBy = "postId", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val images: List<CommunityPostImage> = mutableListOf(),
    
    @OneToMany(mappedBy = "postId", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val likes: List<CommunityLike> = mutableListOf(),
    
    @OneToMany(mappedBy = "postId", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val comments: List<Comment> = mutableListOf()
) {
    fun incrementLikeCount() { likeCount++ }
    fun decrementLikeCount() { if (likeCount > 0) likeCount-- }
    fun incrementCommentCount() { commentCount++ }
    fun decrementCommentCount() { if (commentCount > 0) commentCount-- }

    fun delete() { deletedAt = LocalDateTime.now() }

    fun update(title: String, content: String) {
        this.title = title
        this.content = content
    }

    fun isDeleted() = deletedAt != null
}