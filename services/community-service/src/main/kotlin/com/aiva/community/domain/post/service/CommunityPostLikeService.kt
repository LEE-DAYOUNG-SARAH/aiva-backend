package com.aiva.community.domain.post.service

import com.aiva.community.domain.post.dto.LikeResponse
import com.aiva.community.domain.post.repository.CommunityLikeRepository
import com.aiva.community.domain.post.entity.CommunityLike
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class CommunityPostLikeService(
    private val communityLikeRepository: CommunityLikeRepository,
    private val communityPostReadService: CommunityPostReadService,
    private val communityPostUpdateService: CommunityPostUpdateService
) {
    val log = KotlinLogging.logger {  }

    @Transactional
    fun likePost(postId: UUID, userId: UUID): LikeResponse {
        // Verify post exists and is active
        communityPostReadService.getActivePostById(postId)
        
        val existingLike = communityLikeRepository.findByPostIdAndUserId(postId, userId)
        
        if (existingLike.isPresent) {
            return LikeResponse(isLiked = true, likeCount = communityLikeRepository.countByPostId(postId))
        }
        
        val like = CommunityLike(
            postId = postId,
            userId = userId
        )
        
        communityLikeRepository.save(like)
        communityPostUpdateService.incrementLikeCount(postId)
        
        return LikeResponse(isLiked = true, likeCount = communityLikeRepository.countByPostId(postId))
    }

    @Transactional
    fun unlikePost(postId: UUID, userId: UUID): LikeResponse {
        // Verify post exists and is active
        communityPostReadService.getActivePostById(postId)
        
        val existingLike = communityLikeRepository.findByPostIdAndUserId(postId, userId)
        
        if (existingLike.isEmpty) {
            return LikeResponse(isLiked = false, likeCount = communityLikeRepository.countByPostId(postId))
        }
        
        communityLikeRepository.deleteByPostIdAndUserId(postId, userId)
        communityPostUpdateService.decrementLikeCount(postId)
        
        return LikeResponse(isLiked = false, likeCount = communityLikeRepository.countByPostId(postId))
    }

    @Transactional
    fun togglePostLike(postId: UUID, userId: UUID): LikeResponse {
        val isLiked = communityLikeRepository.existsByPostIdAndUserId(postId, userId)

        return if (isLiked) {
            unlikePost(postId, userId)
        } else {
            likePost(postId, userId)
        }
    }
}