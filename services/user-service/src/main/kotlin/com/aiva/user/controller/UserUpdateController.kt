package com.aiva.user.controller

import com.aiva.user.dto.UserAvatarUpdateRequest
import com.aiva.user.dto.UserAvatarUpdateResponse
import com.aiva.user.service.UserUpdateService
import com.aiva.common.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserUpdateController(
    private val userUpdateService: UserUpdateService
) {
    @PatchMapping("/avatar")
    fun uploadAvatarImage(
        @RequestHeader("X-User-Id") userId: String,
        @Valid @RequestBody request: UserAvatarUpdateRequest
    ): ApiResponse<UserAvatarUpdateResponse> = ApiResponse.success(
        userUpdateService.uploadAvatarImage(userId, request)
    )

    @DeleteMapping("/avatar")
    fun deleteAvatarImage(
        @RequestHeader("X-User-Id") userId: String
    ): ApiResponse<Unit> = ApiResponse.success(
        userUpdateService.deleteAvatarImage(userId)
    )
}
