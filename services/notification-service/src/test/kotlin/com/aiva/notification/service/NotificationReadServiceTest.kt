package com.aiva.notification.service

import com.aiva.notification.dto.ReadAllRequest
import com.aiva.notification.repository.NotificationRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class NotificationReadServiceTest {
    
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var notificationService: NotificationService
    
    @BeforeEach
    fun setUp() {
        notificationRepository = mockk()
        notificationService = NotificationService(notificationRepository)
    }
    
    @Test
    fun `markAllAsRead should mark all unread notifications as read`() {
        // Given
        val userId = UUID.randomUUID()
        val request = ReadAllRequest()
        
        every { 
            notificationRepository.markAllAsRead(
                userId, 
                any<LocalDateTime>(), 
                null
            ) 
        } returns 5
        
        // When
        val result = notificationService.markAllAsRead(request, userId)
        
        // Then
        assertEquals(5, result.readCount)
        
        verify { 
            notificationRepository.markAllAsRead(
                userId, 
                any<LocalDateTime>(), 
                null
            ) 
        }
    }
    
    @Test
    fun `markAllAsRead should respect beforeDate parameter`() {
        // Given
        val userId = UUID.randomUUID()
        val beforeDateStr = "2024-01-01T00:00:00"
        val request = ReadAllRequest(beforeDate = beforeDateStr)
        
        every { 
            notificationRepository.markAllAsRead(
                userId, 
                any<LocalDateTime>(), 
                any<LocalDateTime>()
            ) 
        } returns 3
        
        // When
        val result = notificationService.markAllAsRead(request, userId)
        
        // Then
        assertEquals(3, result.readCount)
        
        verify { 
            notificationRepository.markAllAsRead(
                userId, 
                any<LocalDateTime>(), 
                any<LocalDateTime>()
            ) 
        }
    }
    
}