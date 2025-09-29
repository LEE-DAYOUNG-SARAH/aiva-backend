package com.aiva.common.logging

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.util.*

/**
 * HTTP 요청에 대한 로깅 컨텍스트를 자동으로 설정하는 인터셉터
 * 
 * 모든 HTTP 요청에 대해 MDC 컨텍스트를 초기화하고,
 * 요청 완료 시 정리합니다.
 */
@Component
class LoggingInterceptor : HandlerInterceptor {
    
    private val logger = KotlinLogging.logger {}
    
    companion object {
        private const val START_TIME_ATTRIBUTE = "startTime"
        private const val X_CORRELATION_ID = "X-Correlation-ID"
        private const val X_REQUEST_ID = "X-Request-ID"
        private const val X_FORWARDED_FOR = "X-Forwarded-For"
        private const val X_REAL_IP = "X-Real-IP"
    }
    
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val startTime = System.currentTimeMillis()
        request.setAttribute(START_TIME_ATTRIBUTE, startTime)
        
        // 헤더에서 Correlation ID와 Request ID 추출 또는 생성
        val correlationId = request.getHeader(X_CORRELATION_ID) ?: UUID.randomUUID().toString()
        val requestId = request.getHeader(X_REQUEST_ID) ?: UUID.randomUUID().toString()
        
        // 서비스 이름 결정 (application.yml에서 가져오거나 기본값 사용)
        val serviceName = System.getProperty("spring.application.name") ?: "unknown-service"
        
        // MDC 컨텍스트 초기화
        LoggingContext.initializeContext(
            correlationId = correlationId,
            requestId = requestId,
            serviceName = serviceName
        )
        
        // HTTP 요청 정보 설정
        LoggingContext.setRequestContext(
            endpoint = "${request.method} ${request.requestURI}",
            method = request.method,
            clientIp = getClientIpAddress(request),
            userAgent = request.getHeader("User-Agent")
        )
        
        // 응답 헤더에 추적 정보 추가
        response.setHeader(X_CORRELATION_ID, correlationId)
        response.setHeader(X_REQUEST_ID, requestId)
        
        // 요청 시작 로그
        logger.info { 
            "HTTP Request Started - ${request.method} ${request.requestURI}" +
            if (request.queryString != null) "?${request.queryString}" else ""
        }
        
        return true
    }
    
    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        try {
            val startTime = request.getAttribute(START_TIME_ATTRIBUTE) as? Long
            val executionTime = startTime?.let { System.currentTimeMillis() - it } ?: 0L
            
            // 실행 시간을 MDC에 추가
            LoggingContext.setExecutionTime(executionTime)
            
            // 요청 완료 로그
            val logLevel = when {
                ex != null -> "ERROR"
                response.status >= 500 -> "ERROR"
                response.status >= 400 -> "WARN"
                else -> "INFO"
            }
            
            val logMessage = buildString {
                append("HTTP Request Completed - ")
                append("${request.method} ${request.requestURI} ")
                append("Status: ${response.status} ")
                append("Duration: ${executionTime}ms")
                if (ex != null) {
                    append(" Exception: ${ex.javaClass.simpleName}: ${ex.message}")
                }
            }
            
            when (logLevel) {
                "ERROR" -> logger.error(ex) { logMessage }
                "WARN" -> logger.warn { logMessage }
                else -> logger.info { logMessage }
            }
            
        } finally {
            // MDC 컨텍스트 정리
            LoggingContext.clear()
        }
    }
    
    /**
     * 클라이언트의 실제 IP 주소를 추출
     * 프록시를 거치는 경우를 고려하여 여러 헤더를 확인
     */
    private fun getClientIpAddress(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader(X_FORWARDED_FOR)
        if (!xForwardedFor.isNullOrBlank() && !"unknown".equals(xForwardedFor, ignoreCase = true)) {
            // X-Forwarded-For는 여러 IP가 쉼표로 구분될 수 있음
            return xForwardedFor.split(",")[0].trim()
        }
        
        val xRealIp = request.getHeader(X_REAL_IP)
        if (!xRealIp.isNullOrBlank() && !"unknown".equals(xRealIp, ignoreCase = true)) {
            return xRealIp
        }
        
        return request.remoteAddr ?: "unknown"
    }
}