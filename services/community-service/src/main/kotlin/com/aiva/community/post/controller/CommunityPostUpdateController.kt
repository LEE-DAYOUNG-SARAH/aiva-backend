package com.aiva.community.post.controller

import com.aiva.common.response.ApiResponse
import com.aiva.community.post.dto.LikeResponse
import com.aiva.community.post.dto.UpdatePostRequest
import com.aiva.community.post.service.CommunityPostImageService
import com.aiva.community.post.service.CommunityPostUpdateService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/api/v1/posts")
class CommunityPostUpdateController(
    private val communityPostUpdateService: CommunityPostUpdateService
) {

    @PutMapping("/{postId}")
    fun updatePost(
        @PathVariable postId: UUID,
        @Valid @RequestBody request: UpdatePostRequest,
        principal: Principal
    ): ApiResponse<UUID> {
        val userId = UUID.fromString(principal.name)

        return ApiResponse.success(
            communityPostUpdateService.updatePost(postId, userId, request)
        )
    }

    @DeleteMapping("/{postId}")
    fun deletePost(
        @PathVariable postId: UUID,
        principal: Principal
    ): ApiResponse<UUID> {
        val userId = UUID.fromString(principal.name)

        return ApiResponse.success(
            communityPostUpdateService.deletePost(postId, userId)
        )
    }
}