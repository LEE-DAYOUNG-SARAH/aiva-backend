package com.aiva.notification.service

import com.aiva.notification.domain.notification.repository.NotificationRepository
import com.aiva.notification.domain.notification.repository.NotificationRecipientRepository
import com.aiva.notification.domain.notification.service.NotificationService
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
    private lateinit var notificationRecipientRepository: NotificationRecipientRepository
    private lateinit var notificationService: NotificationService
    
    @BeforeEach
    fun setUp() {
        notificationRepository = mockk()
        notificationRecipientRepository = mockk()
        notificationService = NotificationService(notificationRepository, notificationRecipientRepository)
    }
    
    @Test
    fun `markAllAsRead should mark all unread notifications as read`() {
        // Given
        val userId = UUID.randomUUID()
        
        every { 
            notificationRepository.markAllAsRead(
                userId, 
                any<LocalDateTime>(), 
                any<LocalDateTime>()
            ) 
        } returns 5
        
        // When
        val result = notificationService.markAllAsRead(userId)
        
        // Then
        assertEquals(5, result.readCount)
        
        verify { 
            notificationRepository.markAllAsRead(
                userId, 
                any<LocalDateTime>(), 
                any<LocalDateTime>()
            ) 
        }
    }
    
    @Test
    fun `markAllAsRead should use one month ago as beforeDate`() {
        // Given
        val userId = UUID.randomUUID()
        
        every { 
            notificationRepository.markAllAsRead(
                userId, 
                any<LocalDateTime>(), 
                any<LocalDateTime>()
            ) 
        } returns 3
        
        // When
        val result = notificationService.markAllAsRead(userId)
        
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