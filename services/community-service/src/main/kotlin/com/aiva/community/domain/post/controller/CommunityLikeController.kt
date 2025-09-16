package com.aiva.community.domain.post.controller

import com.aiva.common.response.ApiResponse
import com.aiva.community.domain.post.dto.LikeResponse
import com.aiva.community.domain.post.service.CommunityPostLikeService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/api/v1/posts")
class CommunityLikeController(
    private val communityPostLikeService: CommunityPostLikeService
) {
    @PostMapping("/{postId}/like")
    fun togglePostLike(
        @PathVariable postId: UUID,
        principal: Principal
    ): ApiResponse<LikeResponse> {
        val userId = UUID.fromString(principal.name)

        return ApiResponse.success(
            communityPostLikeService.togglePostLike(postId, userId)
        )
    }
}