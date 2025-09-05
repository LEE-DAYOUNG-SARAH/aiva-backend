package com.aiva.community.comment.controller

import com.aiva.common.response.ApiResponse
import com.aiva.community.comment.dto.UpdateCommentRequest
import com.aiva.community.comment.service.CommentUpdateService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/api/v1")
class CommentUpdateController(
    private val commentUpdateService: CommentUpdateService
) {

    @PutMapping("/comments/{commentId}")
    fun updateComment(
        @PathVariable commentId: UUID,
        @Valid @RequestBody request: UpdateCommentRequest,
        principal: Principal
    ): ApiResponse<UUID> {
        val userId = UUID.fromString(principal.name)

        return ApiResponse.success(
            commentUpdateService.updateComment(commentId, userId, request)
        )
    }

    @DeleteMapping("/comments/{commentId}")
    fun deleteComment(
        @PathVariable commentId: UUID,
        principal: Principal
    ): ApiResponse<Unit> {
        val userId = UUID.fromString(principal.name)
        commentUpdateService.deleteComment(commentId, userId)
        
        return ApiResponse.success()
    }
}