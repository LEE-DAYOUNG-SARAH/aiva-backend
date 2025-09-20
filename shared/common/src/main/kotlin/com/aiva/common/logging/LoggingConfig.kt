package com.aiva.common.logging

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * 로깅 관련 Spring MVC 설정
 * 
 * LoggingInterceptor를 모든 요청에 자동으로 적용합니다.
 */
@Configuration
class LoggingConfig(
    @Autowired private val loggingInterceptor: LoggingInterceptor
) : WebMvcConfigurer {
    
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(loggingInterceptor)
            .addPathPatterns("/**")  // 모든 경로에 적용
            .excludePathPatterns(
                "/actuator/**",      // Actuator 엔드포인트 제외
                "/health/**",        // Health check 제외
                "/favicon.ico",      // Favicon 제외
                "/static/**",        // 정적 리소스 제외
                "/css/**",
                "/js/**",
                "/images/**"
            )
    }
}