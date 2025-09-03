package com.aiva.chat.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {
    
    @Value("\${ai.server.base-url:http://localhost:8080}")
    private lateinit var aiServerBaseUrl: String
    
    @Bean("aiWebClient")
    fun aiWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl(aiServerBaseUrl)
            .codecs { configurer ->
                configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) // 16MB
            }
            .build()
    }
}