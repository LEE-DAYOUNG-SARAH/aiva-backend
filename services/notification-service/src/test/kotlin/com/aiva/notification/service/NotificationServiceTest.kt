package com.aiva.notification.service

import com.aiva.notification.entity.Notification
import com.aiva.notification.entity.NotificationRecipient
import com.aiva.notification.entity.NotificationType
import com.aiva.notification.repository.NotificationRecipientRepository
import com.aiva.notification.repository.NotificationRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime
import java.util.*

class NotificationServiceTest {
    
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var notificationRecipientRepository: NotificationRecipientRepository
    private lateinit var notificationService: NotificationService
    
    @BeforeEach
    fun setUp() {
        notificationRepository = mockk()
        notificationRecipientRepository = mockk()
        notificationService = NotificationService(
            notificationRepository,
            notificationRecipientRepository
        )
    }
    
    @Test
    fun `getUserNotifications should return notifications for last month`() {
        // Given
        val userId = UUID.randomUUID()
        val notificationId = UUID.randomUUID()
        val now = LocalDateTime.now()
        
        val notification = Notification(
            id = notificationId,
            type = NotificationType.COMMUNITY_COMMENT,
            title = "Test Title",
            body = "Test Body",
            createdAt = now.minusDays(10),
            updatedAt = now.minusDays(10)
        )
        
        val recipient = NotificationRecipient(
            notificationId = notificationId,
            userId = userId,
            isRead = false,
            createdAt = now.minusDays(10),
            updatedAt = now.minusDays(10)
        )
        
        val page = PageImpl(listOf(notification))
        val pageable = PageRequest.of(0, 20)
        
        every { 
            notificationRepository.findByUserIdAndCreatedAtAfter(
                userId, 
                any<LocalDateTime>(), 
                pageable
            ) 
        } returns page
        
        every { 
            notificationRecipientRepository.findByUserIdAndNotificationIds(
                userId, 
                listOf(notificationId)
            ) 
        } returns listOf(recipient)
        
        every { 
            notificationRepository.countByUserIdAndCreatedAtAfter(
                userId, 
                any<LocalDateTime>()
            ) 
        } returns 1L
        
        // When
        val result = notificationService.getUserNotifications(userId)
        
        // Then
        assertEquals(1, result.notifications.size)
        assertEquals("Test Title", result.notifications[0].title)
        assertEquals(false, result.notifications[0].isRead)
        assertEquals(1, result.totalCount)
        assertEquals(false, result.hasNext)
    }
    
    @Test
    fun `markAsRead should update notification recipient`() {
        // Given
        val notificationId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        
        every { 
            notificationRecipientRepository.markAsRead(
                notificationId,
                userId,
                any<LocalDateTime>()
            ) 
        } returns 1
        
        // When
        notificationService.markAsRead(notificationId, userId)
        
        // Then
        verify { 
            notificationRecipientRepository.markAsRead(
                notificationId,
                userId,
                any<LocalDateTime>()
            ) 
        }
    }
}