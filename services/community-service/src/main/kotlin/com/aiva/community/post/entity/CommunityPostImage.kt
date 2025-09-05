package com.aiva.community.post.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "community_post_images")
@EntityListeners(AuditingEntityListener::class)
data class CommunityPostImage(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "post_id", nullable = false)
    val postId: UUID,

    @Column(name = "url", nullable = false, columnDefinition = "500")
    val url: String,

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)