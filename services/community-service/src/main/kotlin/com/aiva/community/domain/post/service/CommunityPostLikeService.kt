package com.aiva.community.domain.post.service

import com.aiva.community.domain.post.dto.LikeResponse
import com.aiva.community.domain.post.repository.CommunityLikeRepository
import com.aiva.community.domain.post.entity.CommunityLike
import com.aiva.community.domain.user.UserProfileProjectionRepository
import com.aiva.community.global.event.notification.NotificationEventService
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class CommunityPostLikeService(
    private val communityLikeRepository: CommunityLikeRepository,
    private val communityPostReadService: CommunityPostReadService,
    private val communityPostUpdateService: CommunityPostUpdateService,
    private val userProfileRepository: UserProfileProjectionRepository,
    private val notificationEventService: NotificationEventService
) {
    val log = KotlinLogging.logger {  }

    @Transactional
    fun likePost(postId: UUID, userId: UUID): LikeResponse {
        // Verify post exists and is active
        val post = communityPostReadService.getActivePostById(postId)
        
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
        
        // 게시글 좋아요 알림 이벤트 발행
        try {
            val likerProfile = userProfileRepository.findById(userId).orElse(null)
            if (likerProfile != null) {
                notificationEventService.publishPostLikedNotification(
                    postAuthorId = post.userId,
                    likerUserId = userId,
                    postId = postId,
                    postTitle = post.title,
                    likerNickname = likerProfile.nickname
                )
                log.debug { "Published post liked notification: postId=$postId, liker=$userId" }
            } else {
                log.warn { "User profile not found for like notification: userId=$userId" }
            }
        } catch (e: Exception) {
            log.error(e) { "Failed to publish post liked notification: postId=$postId, userId=$userId" }
            // 알림 실패가 좋아요 기능을 방해하지 않도록 예외를 삼킴
        }
        
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