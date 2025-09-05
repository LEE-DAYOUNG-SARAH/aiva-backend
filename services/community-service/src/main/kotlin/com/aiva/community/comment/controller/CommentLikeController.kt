package com.aiva.community.comment.controller

import com.aiva.common.response.ApiResponse
import com.aiva.community.comment.service.CommentLikeService
import com.aiva.community.post.dto.LikeResponse
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/api/v1")
class CommentLikeController(
    private val commentLikeService: CommentLikeService
) {

    @PostMapping("/comments/{commentId}/like")
    fun toggleCommentLike(
        @PathVariable commentId: UUID,
        principal: Principal
    ): ApiResponse<LikeResponse> {
        val userId = UUID.fromString(principal.name)

        return ApiResponse.success(
            commentLikeService.toggleCommentLike(commentId, userId)
        )
    }
}