package com.aiva.notification.domain.notification.service

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class FcmService(
    private val firebaseMessaging: FirebaseMessaging
) {
    private val logger = KotlinLogging.logger {}
    
    suspend fun sendNotification(
        fcmToken: String,
        title: String,
        body: String,
        imageUrl: String? = null,
        linkUrl: String? = null,
        data: Map<String, String> = emptyMap()
    ): String {
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
            
            withContext(Dispatchers.IO) {
                val result = firebaseMessaging.send(message)
                logger.info { "FCM message sent successfully: $result" }
                result
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to send FCM message to token: $fcmToken" }
            throw e
        }
    }
    
    suspend fun sendBatchNotifications(
        tokens: List<String>,
        title: String,
        body: String,
        imageUrl: String? = null,
        linkUrl: String? = null,
        data: Map<String, String> = emptyMap()
    ): List<String> = coroutineScope {
        val results = tokens.map { token ->
            async {
                runCatching {
                    sendNotification(
                        fcmToken = token, 
                        title = title, 
                        body = body, 
                        imageUrl = imageUrl, 
                        linkUrl = linkUrl, 
                        data = data
                    )
                }.onFailure { e ->
                    logger.warn(e) { "Failed to send FCM to token: $token" }
                }.getOrNull()
            }
        }.awaitAll()
        
        val successResults = results.filterNotNull()
        logger.info { "Batch FCM send completed: ${successResults.size}/${tokens.size} successful" }
        successResults
    }
}