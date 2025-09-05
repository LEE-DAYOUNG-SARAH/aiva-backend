package com.aiva.community.post.service

import com.aiva.community.post.entity.CommunityPost
import com.aiva.community.post.repository.CommunityPostRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class CommunityPostReadService(
    private val communityPostRepository: CommunityPostRepository
) {

    fun getActivePostById(postId: UUID): CommunityPost {
        return communityPostRepository.findActivePostById(postId)
            .orElseThrow { IllegalArgumentException("Post not found or deleted: $postId") }
    }

    fun getActivePosts(pageable: Pageable): Page<CommunityPost> {
        return communityPostRepository.findActivePosts(pageable)
    }

    fun getPopularPosts(pageable: Pageable): Page<CommunityPost> {
        return communityPostRepository.findPopularPosts(pageable)
    }

    fun getUserPosts(userId: UUID, pageable: Pageable): Page<CommunityPost> {
        return communityPostRepository.findActivePostsByUserId(userId, pageable)
    }

    fun getUserPostCount(userId: UUID): Long {
        return communityPostRepository.countActivePostsByUserId(userId)
    }
}