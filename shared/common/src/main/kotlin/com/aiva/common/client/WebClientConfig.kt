package com.aiva.common.client

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

@Configuration
class WebClientConfig(
    @Value("\${app.webclient.services.user-service.url:http://localhost:8081}")
    private val userServiceUrl: String,
    @Value("\${app.webclient.services.notification-service.url:http://localhost:8084}")
    private val notificationServiceUrl: String
) {


    @Bean(USER_SERVICE_WEB_CLIENT)
    fun userServiceWebClient(
        @Qualifier(CLIENT_HTTP_CONNECTOR) connector: ClientHttpConnector
    ) = WebClient.builder()
        .clientConnector(connector)
        .baseUrl(userServiceUrl)
        .build()

    @Bean(NOTIFICATION_SERVICE_WEB_CLIENT)
    fun notificationServiceWebClient(
        @Qualifier(CLIENT_HTTP_CONNECTOR) connector: ClientHttpConnector
    ) = WebClient.builder()
        .clientConnector(connector)
        .baseUrl(notificationServiceUrl)
        .build()

    @Bean(CLIENT_HTTP_CONNECTOR)
    fun connector(): ClientHttpConnector {
        val provider = ConnectionProvider.builder("webclient-connection-pool")
            .maxConnections(100)
            .pendingAcquireTimeout(Duration.ofSeconds(10))
            .pendingAcquireMaxCount(-1)
            .maxIdleTime(Duration.ofMinutes(1))
            .build()
        val client = HttpClient.create(provider)

        return ReactorClientHttpConnector(client)
    }

    companion object {
        const val USER_SERVICE_WEB_CLIENT = "userServiceWebClient"
        const val NOTIFICATION_SERVICE_WEB_CLIENT = "notificationServiceWebClient"
        private const val CLIENT_HTTP_CONNECTOR = "clientHttpConnector"
    }
}