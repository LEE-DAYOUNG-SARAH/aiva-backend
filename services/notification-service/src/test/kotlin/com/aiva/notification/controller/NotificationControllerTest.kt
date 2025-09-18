package com.aiva.notification.controller

import com.aiva.notification.dto.NotificationListResponse
import com.aiva.notification.dto.NotificationResponse
import com.aiva.notification.entity.NotificationType
import com.aiva.notification.service.NotificationService
import com.aiva.security.dto.UserPrincipal
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime
import java.util.*

@WebMvcTest(NotificationController::class)
class NotificationControllerTest {
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    @MockkBean
    private lateinit var notificationService: NotificationService
    
    @Test
    @WithMockUser(roles = ["USER"])
    fun `getUserNotifications should return notification list`() {
        // Given
        val userId = UUID.randomUUID()
        val userPrincipal = UserPrincipal(userId, "test@example.com", "USER")
        
        val notificationResponse = NotificationResponse(
            id = UUID.randomUUID(),
            type = NotificationType.COMMUNITY_COMMENT,
            title = "Test Title",
            body = "Test Body",
            imageUrl = null,
            linkUrl = null,
            isRead = false,
            readAt = null,
            createdAt = LocalDateTime.now()
        )
        
        val response = NotificationListResponse(
            notifications = listOf(notificationResponse),
            totalCount = 1,
            hasNext = false
        )
        
        every { 
            notificationService.getUserNotifications(userId, 0, 20) 
        } returns response
        
        // When & Then
        mockMvc.perform(
            get("/api/notifications")
                .with(user(userPrincipal))
                .param("page", "0")
                .param("size", "20")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.notifications").isArray)
        .andExpect(jsonPath("$.data.notifications[0].title").value("Test Title"))
        .andExpect(jsonPath("$.data.totalCount").value(1))
        .andExpect(jsonPath("$.data.hasNext").value(false))
        
        verify { notificationService.getUserNotifications(userId, 0, 20) }
    }
    
    @Test
    @WithMockUser(roles = ["USER"])
    fun `markAsRead should update notification status`() {
        // Given
        val userId = UUID.randomUUID()
        val notificationId = UUID.randomUUID()
        val userPrincipal = UserPrincipal(userId, "test@example.com", "USER")
        
        every { 
            notificationService.markAsRead(notificationId, userId) 
        } returns Unit
        
        // When & Then
        mockMvc.perform(
            patch("/api/notifications/{notificationId}/read", notificationId)
                .with(user(userPrincipal))
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        
        verify { notificationService.markAsRead(notificationId, userId) }
    }
}