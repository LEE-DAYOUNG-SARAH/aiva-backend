package com.aiva.community.domain.post.controller

import com.aiva.common.response.ApiResponse
import com.aiva.community.domain.post.dto.CreatePostRequest
import com.aiva.community.domain.post.service.CommunityPostCreateService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/api/v1/posts")
class CommunityPostCreateController(
    private val communityPostCreateService: CommunityPostCreateService,
) {

    @PostMapping
    fun createPost(
        @Valid @RequestBody request: CreatePostRequest,
        @RequestHeader("X-User-Id") userId: String,
        @RequestHeader("X-User-Nickname") nickname: String,
        @RequestHeader(value = "X-User-Profile-Url", required = false) profileUrl: String?
    ): ApiResponse<UUID> {
        val userUuid = UUID.fromString(userId)

        return ApiResponse.success(
            communityPostCreateService.createPost(userUuid, nickname, profileUrl, request)
        )
    }
}