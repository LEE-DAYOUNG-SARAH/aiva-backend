package com.aiva.common.logging

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogger
import mu.KotlinLogging
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 구조화된 로깅을 위한 유틸리티 클래스
 * 
 * JSON 형태의 구조화된 로그 메시지를 생성하고,
 * 비즈니스 이벤트별로 표준화된 로깅을 제공합니다.
 */
class StructuredLogger(
    private val logger: KLogger,
    private val objectMapper: ObjectMapper = ObjectMapper()
) {
    
    companion object {
        fun create(clazz: Class<*>): StructuredLogger {
            return StructuredLogger(KotlinLogging.logger(clazz.name))
        }
        
        fun create(name: String): StructuredLogger {
            return StructuredLogger(KotlinLogging.logger(name))
        }
    }
    
    /**
     * 비즈니스 이벤트 로깅
     */
    fun logBusinessEvent(
        eventType: String,
        message: String,
        details: Map<String, Any> = emptyMap(),
        level: LogLevel = LogLevel.INFO
    ) {
        val logData = BusinessEventLog(
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            eventType = eventType,
            message = message,
            correlationId = LoggingContext.getCorrelationId(),
            requestId = LoggingContext.getRequestId(),
            userId = LoggingContext.getUserId(),
            serviceName = LoggingContext.get(LoggingContext.SERVICE_NAME),
            details = details
        )
        
        logWithLevel(level, objectMapper.writeValueAsString(logData))
    }
    
    /**
     * API 호출 로깅
     */
    fun logApiCall(
        apiName: String,
        endpoint: String,
        method: String,
        statusCode: Int,
        duration: Long,
        requestSize: Long? = null,
        responseSize: Long? = null
    ) {
        val logData = ApiCallLog(
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            apiName = apiName,
            endpoint = endpoint,
            method = method,
            statusCode = statusCode,
            duration = duration,
            requestSize = requestSize,
            responseSize = responseSize,
            correlationId = LoggingContext.getCorrelationId(),
            requestId = LoggingContext.getRequestId(),
            userId = LoggingContext.getUserId(),
            serviceName = LoggingContext.get(LoggingContext.SERVICE_NAME)
        )
        
        val level = when {
            statusCode >= 500 -> LogLevel.ERROR
            statusCode >= 400 -> LogLevel.WARN
            else -> LogLevel.INFO
        }
        
        logWithLevel(level, objectMapper.writeValueAsString(logData))
    }
    
    /**
     * 에러 로깅
     */
    fun logError(
        error: Throwable,
        message: String = error.message ?: "Unknown error",
        context: Map<String, Any> = emptyMap()
    ) {
        val logData = ErrorLog(
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            message = message,
            errorType = error.javaClass.simpleName,
            stackTrace = error.stackTraceToString(),
            correlationId = LoggingContext.getCorrelationId(),
            requestId = LoggingContext.getRequestId(),
            userId = LoggingContext.getUserId(),
            serviceName = LoggingContext.get(LoggingContext.SERVICE_NAME),
            context = context
        )
        
        logger.error(error) { objectMapper.writeValueAsString(logData) }
    }
    
    /**
     * 성능 메트릭 로깅
     */
    fun logPerformanceMetric(
        operation: String,
        duration: Long,
        success: Boolean = true,
        metrics: Map<String, Any> = emptyMap()
    ) {
        val logData = PerformanceLog(
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            operation = operation,
            duration = duration,
            success = success,
            correlationId = LoggingContext.getCorrelationId(),
            requestId = LoggingContext.getRequestId(),
            userId = LoggingContext.getUserId(),
            serviceName = LoggingContext.get(LoggingContext.SERVICE_NAME),
            metrics = metrics
        )
        
        logWithLevel(LogLevel.INFO, objectMapper.writeValueAsString(logData))
    }
    
    /**
     * 보안 관련 이벤트 로깅
     */
    fun logSecurityEvent(
        eventType: SecurityEventType,
        message: String,
        severity: SecuritySeverity = SecuritySeverity.MEDIUM,
        details: Map<String, Any> = emptyMap()
    ) {
        val logData = SecurityEventLog(
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            eventType = eventType.name,
            message = message,
            severity = severity.name,
            correlationId = LoggingContext.getCorrelationId(),
            requestId = LoggingContext.getRequestId(),
            userId = LoggingContext.getUserId(),
            serviceName = LoggingContext.get(LoggingContext.SERVICE_NAME),
            clientIp = LoggingContext.get(LoggingContext.CLIENT_IP),
            userAgent = LoggingContext.get(LoggingContext.USER_AGENT),
            details = details
        )
        
        val level = when (severity) {
            SecuritySeverity.LOW -> LogLevel.INFO
            SecuritySeverity.MEDIUM -> LogLevel.WARN
            SecuritySeverity.HIGH -> LogLevel.ERROR
            SecuritySeverity.CRITICAL -> LogLevel.ERROR
        }
        
        logWithLevel(level, objectMapper.writeValueAsString(logData))
    }
    
    private fun logWithLevel(level: LogLevel, message: String) {
        when (level) {
            LogLevel.TRACE -> logger.trace { message }
            LogLevel.DEBUG -> logger.debug { message }
            LogLevel.INFO -> logger.info { message }
            LogLevel.WARN -> logger.warn { message }
            LogLevel.ERROR -> logger.error { message }
        }
    }
}

enum class LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR
}

enum class SecurityEventType {
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    LOGOUT,
    PASSWORD_CHANGE,
    ACCOUNT_LOCKED,
    UNAUTHORIZED_ACCESS,
    TOKEN_EXPIRED,
    SUSPICIOUS_ACTIVITY,
    DATA_ACCESS,
    PERMISSION_DENIED
}

enum class SecuritySeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

/**
 * 로그 데이터 클래스들
 */
data class BusinessEventLog(
    val timestamp: String,
    val eventType: String,
    val message: String,
    val correlationId: String?,
    val requestId: String?,
    val userId: String?,
    val serviceName: String?,
    val details: Map<String, Any>
)

data class ApiCallLog(
    val timestamp: String,
    val apiName: String,
    val endpoint: String,
    val method: String,
    val statusCode: Int,
    val duration: Long,
    val requestSize: Long?,
    val responseSize: Long?,
    val correlationId: String?,
    val requestId: String?,
    val userId: String?,
    val serviceName: String?
)

data class ErrorLog(
    val timestamp: String,
    val message: String,
    val errorType: String,
    val stackTrace: String,
    val correlationId: String?,
    val requestId: String?,
    val userId: String?,
    val serviceName: String?,
    val context: Map<String, Any>
)

data class PerformanceLog(
    val timestamp: String,
    val operation: String,
    val duration: Long,
    val success: Boolean,
    val correlationId: String?,
    val requestId: String?,
    val userId: String?,
    val serviceName: String?,
    val metrics: Map<String, Any>
)

data class SecurityEventLog(
    val timestamp: String,
    val eventType: String,
    val message: String,
    val severity: String,
    val correlationId: String?,
    val requestId: String?,
    val userId: String?,
    val serviceName: String?,
    val clientIp: String?,
    val userAgent: String?,
    val details: Map<String, Any>
)