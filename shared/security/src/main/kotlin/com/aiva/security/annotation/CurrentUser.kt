package com.aiva.security.annotation

import com.aiva.security.dto.UserPrincipal
import com.aiva.security.util.SecurityContextUtil
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.util.*

/**
 * @CurrentUser 어노테이션
 * 컨트롤러 메서드에서 현재 사용자 정보를 쉽게 받을 수 있도록 함
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class CurrentUser

/**
 * CurrentUser 어노테이션 처리기
 */
@Component
class CurrentUserArgumentResolver(
    private val securityContextUtil: SecurityContextUtil
) : HandlerMethodArgumentResolver {
    
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(CurrentUser::class.java) && 
               (parameter.parameterType == UserPrincipal::class.java || 
                parameter.parameterType == UUID::class.java)
    }
    
    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        return when (parameter.parameterType) {
            UserPrincipal::class.java -> securityContextUtil.getCurrentUser()
            UUID::class.java -> securityContextUtil.getCurrentUserId()
            else -> null
        }
    }
}