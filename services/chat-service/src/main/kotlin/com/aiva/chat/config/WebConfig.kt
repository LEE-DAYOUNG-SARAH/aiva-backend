package com.aiva.chat.config

import com.aiva.security.annotation.CurrentUserArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Chat Service Web 설정
 * @CurrentUser 어노테이션 지원
 */
@Configuration
class WebConfig(
    private val currentUserArgumentResolver: CurrentUserArgumentResolver
) : WebMvcConfigurer {
    
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(currentUserArgumentResolver)
    }
}