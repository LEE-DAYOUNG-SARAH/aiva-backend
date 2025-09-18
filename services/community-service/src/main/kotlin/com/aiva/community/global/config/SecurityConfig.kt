package com.aiva.community.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { authorize ->
                authorize
                    // Public read access to posts and comments
                    .requestMatchers(HttpMethod.GET, "/api/v1/posts").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/posts/{postId}").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/posts/{postId}/comments").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/comments/{commentId}/replies").permitAll()
                    
                    // Public access to user posts and comments (for profiles)
                    .requestMatchers(HttpMethod.GET, "/api/v1/posts/users/{userId}").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/users/{userId}/comments").permitAll()
                    
                    // Health and monitoring endpoints
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/api-docs/**").permitAll()
                    
                    // Admin endpoints require authentication (to be enhanced with role-based access)
                    .requestMatchers("/api/v1/reports/admin/**").authenticated()
                    
                    // All other endpoints require authentication
                    .anyRequest().authenticated()
            }
        
        return http.build()
    }
}