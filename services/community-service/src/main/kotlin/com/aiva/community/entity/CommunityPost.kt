package com.aiva.community.entity

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
    
    @Column(name = "content", nullable = false, length = 1000)
    val content: String,
    
    @Column(name = "like_count", nullable = false)
    var likeCount: Int = 0,
    
    @Column(name = "comment_count", nullable = false)
    var commentCount: Int = 0,
    
    @Column(name = "deleted_at")
    val deletedAt: LocalDateTime? = null,
    
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
}

@Entity
@Table(name = "community_post_images")
@EntityListeners(AuditingEntityListener::class)
data class CommunityPostImage(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "post_id", nullable = false)
    val postId: UUID,
    
    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    val url: String,
    
    @Column(name = "width")
    val width: Int? = null,
    
    @Column(name = "height")
    val height: Int? = null,
    
    @Column(name = "mime_type", length = 50)
    val mimeType: String? = null,
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "community_likes")
@EntityListeners(AuditingEntityListener::class)
data class CommunityLike(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "post_id", nullable = false)
    val postId: UUID,
    
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
            UniqueConstraint(columnNames = ["post_id", "user_id"])
        ]
    )
    companion object
}
