package com.aiva.community.domain.post.dto

import com.aiva.common.redis.entity.CommunityPostCache
import com.aiva.community.domain.post.entity.CommunityPost
import java.time.LocalDateTime
import java.util.*

/**
 * 게시물과 작성자 정보를 함께 담는 DTO
 * 
 * gRPC를 통해 조회한 사용자 정보를 포함
 */
data class CommunityPostWithAuthor(
    val post: CommunityPost,
    val imageUrls: List<String> = emptyList(),
    val author: AuthorInfo
) {
    /**
     * API 응답용 DTO로 변환
     */
    fun toCommunityPostResponse(): CommunityPostResponse {
        return CommunityPostResponse(
            id = post.id,
            title = post.title,
            content = post.content,
            imageUrls = imageUrls,
            likeCount = post.likeCount,
            commentCount = post.commentCount,
            createdAt = post.createdAt,
            author = author
        )
    }

    fun toCommunityPostCache() = CommunityPostCache(
        id = post.id,
        title = post.title,
        content = post.content,
        imageUrls = imageUrls,
        likeCount = post.likeCount,
        commentCount = post.commentCount,
        createdAt = post.createdAt,
        updatedAt = post.updatedAt,
        authorId = post.userId
    )
}