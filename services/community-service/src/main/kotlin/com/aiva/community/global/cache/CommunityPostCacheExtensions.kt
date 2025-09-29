package com.aiva.community.global.cache

import com.aiva.common.redis.entity.CommunityPostCache
import com.aiva.community.domain.post.entity.CommunityPost
import com.aiva.community.domain.post.dto.CommunityPostWithAuthor

/**
 * CommunityPostCache를 CommunityPost로 변환하는 extension 함수
 */
fun CommunityPostCache.toCommunityPost(): CommunityPost {
    return CommunityPost(
        id = this.id,
        userId = this.authorId,
        title = this.title,
        content = this.content,
        likeCount = this.likeCount,
        commentCount = this.commentCount,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

/**
 * CommunityPost를 CommunityPostCache로 변환하는 extension 함수
 */
fun CommunityPost.toCommunityPostCache(): CommunityPostCache {
    return CommunityPostCache(
        id = this.id,
        title = this.title,
        content = this.content,
        imageUrls = this.images.map { it.url },
        likeCount = this.likeCount,
        commentCount = this.commentCount,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        authorId = this.userId
    )
}

