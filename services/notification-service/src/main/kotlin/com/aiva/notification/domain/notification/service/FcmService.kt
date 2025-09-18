package com.aiva.notification.domain.notification.service

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class FcmService(
    private val firebaseMessaging: FirebaseMessaging
) {
    private val logger = KotlinLogging.logger {}
    
    fun sendNotification(
        fcmToken: String,
        title: String,
        body: String,
        imageUrl: String? = null,
        linkUrl: String? = null,
        data: Map<String, String> = emptyMap()
    ): CompletableFuture<String> {
        return try {
            val notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .apply { imageUrl?.let { setImage(it) } }
                .build()
            
            val messageBuilder = Message.builder()
                .setToken(fcmToken)
                .setNotification(notification)
                .putAllData(data)
            
            // 딥링크나 웹링크 처리
            linkUrl?.let { url ->
                if (url.startsWith("http")) {
                    messageBuilder.putData("link", url)
                } else {
                    messageBuilder.putData("deeplink", url)
                }
            }
            
            val message = messageBuilder.build()
            
            CompletableFuture.supplyAsync {
                try {
                    val result = firebaseMessaging.send(message)
                    logger.info { "FCM message sent successfully: $result" }
                    result
                } catch (e: Exception) {
                    logger.error(e) { "Failed to send FCM message to token: $fcmToken" }
                    throw e
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Error creating FCM message for token: $fcmToken" }
            CompletableFuture.failedFuture(e)
        }
    }
    
    fun sendBatchNotifications(
        tokens: List<String>,
        title: String,
        body: String,
        imageUrl: String? = null,
        linkUrl: String? = null,
        data: Map<String, String> = emptyMap()
    ): CompletableFuture<List<String>> {
        val futures = tokens.map { token ->
            sendNotification(fcmToken = token, title = title, body = body, imageUrl = imageUrl, linkUrl = linkUrl, data = data)
        }
        
        return CompletableFuture.allOf(*futures.toTypedArray())
            .thenApply { futures.map { it.join() } }
    }
}