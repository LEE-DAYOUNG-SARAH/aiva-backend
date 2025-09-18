package com.aiva.notification.domain.notification.controller

import com.aiva.common.response.ApiResponse
import com.aiva.notification.domain.notification.dto.NotificationListResponse
import com.aiva.notification.domain.notification.dto.ReadAllResponse
import com.aiva.notification.domain.notification.service.NotificationService
import com.aiva.security.annotation.CurrentUser
import com.aiva.security.dto.UserPrincipal
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("hasRole('USER')")
class NotificationController(
    private val notificationService: NotificationService
) {
    
    @GetMapping
    fun getUserNotifications(
        @CurrentUser userPrincipal: UserPrincipal,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<NotificationListResponse> {
        val notifications = notificationService.getUserNotifications(
            userId = userPrincipal.userId,
            page = page,
            size = size
        )
        
        return ApiResponse.success(notifications)
    }
    
    @PatchMapping("/{notificationId}/read")
    fun markAsRead(
        @CurrentUser userPrincipal: UserPrincipal,
        @PathVariable notificationId: UUID
    ): ApiResponse<Unit> {
        notificationService.markAsRead(notificationId, userPrincipal.userId)
        return ApiResponse.success()
    }
    
    @PatchMapping("/read-all")
    fun markAllAsRead(
        @CurrentUser userPrincipal: UserPrincipal
    ): ApiResponse<ReadAllResponse> {
        val response = notificationService.markAllAsRead(userPrincipal.userId)
        return ApiResponse.success(response)
    }
}