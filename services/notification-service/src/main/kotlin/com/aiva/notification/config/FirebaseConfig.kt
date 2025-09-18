package com.aiva.notification.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.io.IOException
import javax.annotation.PostConstruct

@Configuration
class FirebaseConfig {
    
    private val logger = KotlinLogging.logger {}
    
    @Value("\${firebase.config.file:firebase-service-account.json}")
    private lateinit var firebaseConfigPath: String
    
    @Value("\${firebase.project.id}")
    private lateinit var projectId: String
    
    @PostConstruct
    fun initFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                val serviceAccount = ClassPathResource(firebaseConfigPath).inputStream
                val credentials = GoogleCredentials.fromStream(serviceAccount)
                
                val options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setProjectId(projectId)
                    .build()
                
                FirebaseApp.initializeApp(options)
                logger.info { "Firebase application initialized successfully" }
            }
        } catch (e: IOException) {
            logger.error(e) { "Failed to initialize Firebase application" }
            throw RuntimeException("Firebase initialization failed", e)
        }
    }
    
    @Bean
    fun firebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }
}