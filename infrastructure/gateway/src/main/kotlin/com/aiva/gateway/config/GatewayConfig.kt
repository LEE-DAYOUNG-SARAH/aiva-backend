package com.aiva.gateway.config

import com.aiva.gateway.filter.JwtAuthenticationFilter
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Gateway 라우팅 설정
 * JWT 인증 필터를 모든 라우트에 적용
 */
@Configuration
class GatewayConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {
    
    @Bean
    fun customRouteLocator(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
            // User Service Routes (인증 포함)
            .route("user-service") { r ->
                r.path("/api/users/**", "/api/children/**", "/api/devices/**", "/api/auth/**")
                    .filters { f ->
                        f.stripPrefix(1)
                            .filter(jwtAuthenticationFilter.apply())
                    }
                    .uri("http://localhost:8081")
            }
            
            // Chat Service Routes (모든 API 인증 필수)
            .route("chat-service") { r ->
                r.path("/api/chats/**", "/api/messages/**", "/api/faqs/**")
                    .filters { f ->
                        f.stripPrefix(1)
                            .filter(jwtAuthenticationFilter.apply())
                    }
                    .uri("http://localhost:8082")
            }
            
            // Community Service Routes (모든 API 인증 필수)
            .route("community-service") { r ->
                r.path("/api/posts/**", "/api/comments/**", "/api/likes/**", "/api/reports/**")
                    .filters { f ->
                        f.stripPrefix(1)
                            .filter(jwtAuthenticationFilter.apply())
                    }
                    .uri("http://localhost:8083")
            }
            
            // Notification Service Routes (모든 API 인증 필수)
            .route("notification-service") { r ->
                r.path("/api/notifications/**", "/api/notification-settings/**")
                    .filters { f ->
                        f.stripPrefix(1)
                            .filter(jwtAuthenticationFilter.apply())
                    }
                    .uri("http://localhost:8084")
            }
            
            // Subscription Service Routes (모든 API 인증 필수)
            .route("subscription-service") { r ->
                r.path("/api/subscriptions/**", "/api/plans/**", "/api/payments/**")
                    .filters { f ->
                        f.stripPrefix(1)
                            .filter(jwtAuthenticationFilter.apply())
                    }
                    .uri("http://localhost:8085")
            }
            
            // Batch Service Routes (관리자 전용, 추후 별도 인증 구현)
            .route("batch-service") { r ->
                r.path("/api/batch/**")
                    .filters { f ->
                        f.stripPrefix(1)
                            .filter(jwtAuthenticationFilter.apply())
                    }
                    .uri("http://localhost:8086")
            }
            .build()
    }
}