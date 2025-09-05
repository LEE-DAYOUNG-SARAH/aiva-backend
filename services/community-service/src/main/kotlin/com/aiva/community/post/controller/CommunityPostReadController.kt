package com.aiva.community.post.controller

import com.aiva.common.response.ApiResponse
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/api/v1/posts")
class CommunityPostReadController() {

    // TODO. 조회 방안 모색 후 적용
    @GetMapping
    fun getCommunityFeed(
        pageable: Pageable,
        principal: Principal
    ): ApiResponse<Unit> {
        val userId = UUID.fromString(principal.name)
        
        return ApiResponse.success()
    }

    @GetMapping("/{postId}")
    fun getPost(
        @PathVariable postId: UUID,
        principal: Principal
    ): ApiResponse<Unit> {
        val userId = UUID.fromString(principal.name)

        return ApiResponse.success()
    }

    @GetMapping("/me")
    fun getUserPosts(
        pageable: Pageable,
        principal: Principal
    ): ApiResponse<Unit> {
        val userId = UUID.fromString(principal.name)

        return ApiResponse.success()
    }
}