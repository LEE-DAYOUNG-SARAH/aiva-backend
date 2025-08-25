package com.aiva.user.user.controller

import com.aiva.user.user.dto.UserAvatarUpdateRequest
import com.aiva.user.user.dto.UserAvatarUpdateResponse
import com.aiva.user.user.service.UserUpdateService
import com.aiva.common.response.ApiResponse
import com.aiva.user.user.dto.UserInfoUpdateRequest
import com.aiva.user.user.dto.UserInfoUpdateResponse
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

    @PutMapping("/me")
    fun updateUserInfo(
        @RequestHeader("X-User-Id") userId: String,
        @Valid @RequestBody request: UserInfoUpdateRequest
    ): ApiResponse<UserInfoUpdateResponse> = ApiResponse.success(
        userUpdateService.updateUserInfo(userId, request)
    )
}
