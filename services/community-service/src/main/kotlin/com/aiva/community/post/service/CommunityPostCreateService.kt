package com.aiva.community.post.service

import com.aiva.community.post.dto.CreatePostRequest
import com.aiva.community.post.entity.CommunityPost
import com.aiva.community.post.repository.CommunityPostRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class CommunityPostCreateService(
    private val communityPostRepository: CommunityPostRepository,
    private val communityPostImageService: CommunityPostImageService,
    private val communityPostReadService: CommunityPostReadService
) {

    fun createPost(userId: UUID, request: CreatePostRequest): UUID {
        val post = communityPostRepository.save(
            CommunityPost(
                userId = userId,
                title = request.title,
                content = request.content
            )
        )

        communityPostImageService.createAll(post.id, request.imageUrls)
        
        return post.id
    }
}