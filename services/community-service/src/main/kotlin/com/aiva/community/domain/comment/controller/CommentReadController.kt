package com.aiva.community.domain.comment.controller

import com.aiva.common.response.ApiResponse
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/api/v1")
class CommentReadController() {

    // TODO. 조회 방안 모색 후 적용
    @GetMapping("/posts/{postId}/comments")
    fun getPostComments(
        pageable: Pageable,
        principal: Principal
    ): ApiResponse<Unit> {
        return ApiResponse.success()
    }

    @GetMapping("/comments/{commentId}/replies")
    fun getCommentReplies(
        @PathVariable commentId: UUID,
        principal: Principal
    ): ApiResponse<Unit> {
        return ApiResponse.success()
    }
}