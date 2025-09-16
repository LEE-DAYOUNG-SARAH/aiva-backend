package com.aiva.community.domain.post.controller

import com.aiva.common.response.ApiResponse
import com.aiva.community.domain.post.dto.CreatePostRequest
import com.aiva.community.domain.post.service.CommunityPostCreateService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
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
        principal: Principal
    ): ApiResponse<UUID> {
        val userId = UUID.fromString(principal.name)

        return ApiResponse.success(
            communityPostCreateService.createPost(userId, request)
        )
    }
}