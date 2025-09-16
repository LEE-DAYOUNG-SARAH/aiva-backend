package com.aiva.community.domain.post.dto

import com.aiva.common.redis.entity.CommunityPostCache
import com.aiva.community.domain.post.entity.CommunityPost
import com.aiva.community.domain.user.AuthorInfo
import java.time.LocalDateTime
import java.util.*

/**
 * 게시물과 작성자 정보를 함께 담는 DTO
 * 
 * 로컬 프로젝션을 통해 외부 서비스 호출 없이 
 * 완전한 게시물 정보를 제공한다.
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
            imageUrls = emptyList(), // TODO: 이미지 정보 추가 시
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
        authorId = author.userId,
        authorNickname = author.nickname,
        authorProfileImageUrl = author.profileImageUrl
    )
}