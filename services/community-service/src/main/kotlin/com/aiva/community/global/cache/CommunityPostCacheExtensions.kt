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
fun CommunityPost.toCommunityPostCache(authorNickname: String = "Unknown", authorProfileImageUrl: String? = null): CommunityPostCache {
    return CommunityPostCache(
        id = this.id,
        title = this.title,
        content = this.content,
        imageUrls = this.images.map { it.url },
        likeCount = this.likeCount,
        commentCount = this.commentCount,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        authorId = this.userId,
        authorNickname = authorNickname,
        authorProfileImageUrl = authorProfileImageUrl
    )
}

/**
 * CommunityPostWithAuthor를 CommunityPostCache로 변환하는 extension 함수
 */
fun CommunityPostWithAuthor.toCommunityPostCache(): CommunityPostCache {
    return CommunityPostCache(
        id = this.post.id,
        title = this.post.title,
        content = this.post.content,
        imageUrls = this.post.images.map { it.url },
        likeCount = this.post.likeCount,
        commentCount = this.post.commentCount,
        createdAt = this.post.createdAt,
        updatedAt = this.post.updatedAt,
        authorId = this.post.userId,
        authorNickname = this.author.nickname,
        authorProfileImageUrl = this.author.profileImageUrl
    )
}