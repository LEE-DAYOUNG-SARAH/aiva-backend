package com.aiva.community.global.event.notification

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.context.ApplicationEventPublisher
import java.util.*

@ExtendWith(MockitoExtension::class)
class NotificationEventServiceTest {

    @Mock
    private lateinit var applicationEventPublisher: ApplicationEventPublisher

    @InjectMocks
    private lateinit var notificationEventService: NotificationEventService

    @Test
    fun `게시글 좋아요 알림을 발행한다`() {
        // given
        val postAuthorId = UUID.randomUUID()
        val likerUserId = UUID.randomUUID()
        val postId = UUID.randomUUID()
        val postTitle = "테스트 게시글"
        val likerNickname = "좋아요유저"

        // when
        notificationEventService.publishPostLikedNotification(
            postAuthorId = postAuthorId,
            likerUserId = likerUserId,
            postId = postId,
            postTitle = postTitle,
            likerNickname = likerNickname
        )

        // then
        verify(applicationEventPublisher).publishEvent(any<NotificationApplicationEvent>())
    }

    @Test
    fun `본인이 본인 게시글에 좋아요 누르면 알림을 발행하지 않는다`() {
        // given
        val userId = UUID.randomUUID()
        val postId = UUID.randomUUID()

        // when
        notificationEventService.publishPostLikedNotification(
            postAuthorId = userId,
            likerUserId = userId,
            postId = postId,
            postTitle = "테스트 게시글",
            likerNickname = "본인"
        )

        // then
        verify(applicationEventPublisher, org.mockito.kotlin.never()).publishEvent(any<NotificationApplicationEvent>())
    }

    @Test
    fun `댓글 작성 알림을 발행한다`() {
        // given
        val postAuthorId = UUID.randomUUID()
        val commenterUserId = UUID.randomUUID()
        val postId = UUID.randomUUID()
        val commentId = UUID.randomUUID()

        // when
        notificationEventService.publishCommentCreatedNotification(
            postAuthorId = postAuthorId,
            commenterUserId = commenterUserId,
            postId = postId,
            commentId = commentId,
            postTitle = "테스트 게시글",
            commenterNickname = "댓글유저",
            commentContent = "테스트 댓글"
        )

        // then
        verify(applicationEventPublisher).publishEvent(any<NotificationApplicationEvent>())
    }

    @Test
    fun `대댓글 작성 알림을 발행한다`() {
        // given
        val originalCommenterUserId = UUID.randomUUID()
        val replierUserId = UUID.randomUUID()
        val postId = UUID.randomUUID()
        val originalCommentId = UUID.randomUUID()
        val replyCommentId = UUID.randomUUID()

        // when
        notificationEventService.publishReplyCreatedNotification(
            originalCommenterUserId = originalCommenterUserId,
            replierUserId = replierUserId,
            postId = postId,
            originalCommentId = originalCommentId,
            replyCommentId = replyCommentId,
            replierNickname = "답글유저",
            replyContent = "테스트 답글"
        )

        // then
        verify(applicationEventPublisher).publishEvent(any<NotificationApplicationEvent>())
    }

    @Test
    fun `댓글 좋아요 알림을 발행한다`() {
        // given
        val commentAuthorId = UUID.randomUUID()
        val likerUserId = UUID.randomUUID()
        val commentId = UUID.randomUUID()
        val postId = UUID.randomUUID()

        // when
        notificationEventService.publishCommentLikedNotification(
            commentAuthorId = commentAuthorId,
            likerUserId = likerUserId,
            commentId = commentId,
            postId = postId,
            likerNickname = "좋아요유저"
        )

        // then
        verify(applicationEventPublisher).publishEvent(any<NotificationApplicationEvent>())
    }
}