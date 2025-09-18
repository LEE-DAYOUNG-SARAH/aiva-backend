package com.aiva.community.domain.comment.controller

import com.aiva.common.response.ApiResponse
import com.aiva.community.domain.comment.dto.CreateCommentRequest
import com.aiva.community.domain.comment.service.CommentCreateService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/api/v1")
class CommentCreateController(
    private val commentCreateService: CommentCreateService
) {

    @PostMapping("/posts/{postId}/comments")
    fun createComment(
        @PathVariable postId: UUID,
        @Valid @RequestBody request: CreateCommentRequest,
        principal: Principal
    ): ApiResponse<UUID> {
        val userId = UUID.fromString(principal.name)

        return ApiResponse.success(
            commentCreateService.createComment(postId, userId, request)
        )
    }

    @PostMapping("/posts/{postId}/comments/{commentId}/replies")
    fun createReply(
        @PathVariable postId: UUID,
        @PathVariable commentId: UUID,
        @Valid @RequestBody request: CreateCommentRequest,
        principal: Principal
    ): ApiResponse<UUID> {
        val userId = UUID.fromString(principal.name)

        return ApiResponse.success(
            commentCreateService.createReply(postId, commentId, userId, request)
        )
    }
}