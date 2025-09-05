package com.aiva.community.post.controller

import com.aiva.common.response.ApiResponse
import com.aiva.community.post.dto.CreatePostRequest
import com.aiva.community.post.service.CommunityPostCreateService
import com.aiva.community.post.service.LikeService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/api/v1/posts")
class CommunityPostCreateController(
    private val communityPostCreateService: CommunityPostCreateService,
    private val likeService: LikeService
) {

    @PostMapping
    fun createPost(
        @Valid @RequestBody request: CreatePostRequest,
        principal: Principal
    ): ApiResponse<UUID> {
        val userId = UUID.fromString(principal.name)

        return ApiResponse.success(
            communityPostCreateService.createPost(userId, request)
        )
    }
}