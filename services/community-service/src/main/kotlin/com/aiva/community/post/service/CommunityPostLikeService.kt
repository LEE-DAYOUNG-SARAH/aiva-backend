package com.aiva.community.post.service

import com.aiva.community.post.dto.LikeResponse
import com.aiva.community.post.entity.CommunityLike
import com.aiva.community.post.repository.CommunityLikeRepository
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
        val isLiked = isPostLikedByUser(postId, userId)

        return if (isLiked) {
            unlikePost(postId, userId)
        } else {
            likePost(postId, userId)
        }
    }

    fun isPostLikedByUser(postId: UUID, userId: UUID): Boolean {
        return communityLikeRepository.existsByPostIdAndUserId(postId, userId)
    }

    fun getPostLikeCount(postId: UUID): Long {
        return communityLikeRepository.countByPostId(postId)
    }
}