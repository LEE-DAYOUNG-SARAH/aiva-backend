package com.aiva.notification.controller

import com.aiva.notification.domain.notification.controller.NotificationController
import com.aiva.notification.domain.notification.dto.ReadAllRequest
import com.aiva.notification.domain.notification.dto.ReadAllResponse
import com.aiva.notification.domain.notification.service.NotificationService
import com.aiva.security.dto.UserPrincipal
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.*

@WebMvcTest(NotificationController::class)
class NotificationReadControllerTest {
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    @MockkBean
    private lateinit var notificationService: NotificationService
    
    @Test
    @WithMockUser(roles = ["USER"])
    fun `markAllAsRead should process read all request successfully`() {
        // Given
        val userId = UUID.randomUUID()
        val userPrincipal = UserPrincipal(userId, "test@example.com", "USER")
        
        val request = ReadAllRequest()
        val response = ReadAllResponse(readCount = 10)
        
        every { 
            notificationService.markAllAsRead(request, userId) 
        } returns response
        
        // When & Then
        mockMvc.perform(
            patch("/api/notifications/read-all")
                .with(user(userPrincipal))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.readCount").value(10))
        
        verify { notificationService.markAllAsRead(request, userId) }
    }
    
    @Test
    @WithMockUser(roles = ["USER"])
    fun `markAllAsRead should handle date parameter correctly`() {
        // Given
        val userId = UUID.randomUUID()
        val userPrincipal = UserPrincipal(userId, "test@example.com", "USER")
        
        val request = ReadAllRequest(beforeDate = "2024-01-01T00:00:00")
        val response = ReadAllResponse(readCount = 5)
        
        every { 
            notificationService.markAllAsRead(request, userId) 
        } returns response
        
        // When & Then
        mockMvc.perform(
            patch("/api/notifications/read-all")
                .with(user(userPrincipal))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.readCount").value(5))
        
        verify { notificationService.markAllAsRead(request, userId) }
    }
    
}