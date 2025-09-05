package com.aiva.community.post.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

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
