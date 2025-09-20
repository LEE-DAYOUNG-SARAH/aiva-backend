package com.aiva.common.logging

import org.slf4j.MDC
import java.util.*

/**
 * MDC 기반 로깅 컨텍스트 관리 유틸리티
 * 
 * 마이크로서비스 간 추적 가능한 로깅을 위한 컨텍스트 정보를 관리합니다.
 * Correlation ID, User ID, Request ID 등을 MDC에 설정하여
 * 모든 로그에 자동으로 포함되도록 합니다.
 */
object LoggingContext {
    
    // MDC 키 상수
    const val CORRELATION_ID = "correlationId"
    const val REQUEST_ID = "requestId"
    const val USER_ID = "userId"
    const val SERVICE_NAME = "serviceName"
    const val API_ENDPOINT = "apiEndpoint"
    const val HTTP_METHOD = "httpMethod"
    const val CLIENT_IP = "clientIp"
    const val USER_AGENT = "userAgent"
    const val EXECUTION_TIME = "executionTime"
    const val TRACE_ID = "traceId"
    const val SPAN_ID = "spanId"
    
    /**
     * 새로운 요청에 대한 로깅 컨텍스트 초기화
     */
    fun initializeContext(
        correlationId: String? = null,
        requestId: String = UUID.randomUUID().toString(),
        serviceName: String
    ) {
        MDC.put(CORRELATION_ID, correlationId ?: UUID.randomUUID().toString())
        MDC.put(REQUEST_ID, requestId)
        MDC.put(SERVICE_NAME, serviceName)
    }
    
    /**
     * 사용자 정보를 컨텍스트에 추가
     */
    fun setUserContext(userId: String) {
        MDC.put(USER_ID, userId)
    }
    
    /**
     * HTTP 요청 정보를 컨텍스트에 추가
     */
    fun setRequestContext(
        endpoint: String,
        method: String,
        clientIp: String? = null,
        userAgent: String? = null
    ) {
        MDC.put(API_ENDPOINT, endpoint)
        MDC.put(HTTP_METHOD, method)
        clientIp?.let { MDC.put(CLIENT_IP, it) }
        userAgent?.let { MDC.put(USER_AGENT, it) }
    }
    
    /**
     * 분산 추적 정보를 컨텍스트에 추가
     */
    fun setTraceContext(traceId: String, spanId: String) {
        MDC.put(TRACE_ID, traceId)
        MDC.put(SPAN_ID, spanId)
    }
    
    /**
     * 실행 시간을 컨텍스트에 추가 (밀리초)
     */
    fun setExecutionTime(executionTimeMs: Long) {
        MDC.put(EXECUTION_TIME, executionTimeMs.toString())
    }
    
    /**
     * Correlation ID 반환
     */
    fun getCorrelationId(): String? = MDC.get(CORRELATION_ID)
    
    /**
     * Request ID 반환
     */
    fun getRequestId(): String? = MDC.get(REQUEST_ID)
    
    /**
     * User ID 반환
     */
    fun getUserId(): String? = MDC.get(USER_ID)
    
    /**
     * 현재 컨텍스트의 모든 정보를 Map으로 반환
     */
    fun getCurrentContext(): Map<String, String> = MDC.getCopyOfContextMap() ?: emptyMap()
    
    /**
     * 특정 키의 값을 설정
     */
    fun put(key: String, value: String) {
        MDC.put(key, value)
    }
    
    /**
     * 특정 키의 값을 반환
     */
    fun get(key: String): String? = MDC.get(key)
    
    /**
     * 컨텍스트 정리 (요청 완료 시 호출)
     */
    fun clear() {
        MDC.clear()
    }
    
    /**
     * 특정 키만 제거
     */
    fun remove(key: String) {
        MDC.remove(key)
    }
    
    /**
     * 다른 스레드로 컨텍스트 전파를 위한 복사본 생성
     */
    fun copyForAsync(): Map<String, String>? = MDC.getCopyOfContextMap()
    
    /**
     * 비동기 작업에서 컨텍스트 복원
     */
    fun restoreFromAsync(contextMap: Map<String, String>?) {
        contextMap?.let { MDC.setContextMap(it) } ?: MDC.clear()
    }
}

/**
 * 블록 실행 중 임시로 MDC 컨텍스트를 설정하는 유틸리티 함수
 */
inline fun <T> withLoggingContext(
    contextMap: Map<String, String>,
    block: () -> T
): T {
    val originalContext = LoggingContext.copyForAsync()
    return try {
        LoggingContext.restoreFromAsync(contextMap)
        block()
    } finally {
        LoggingContext.restoreFromAsync(originalContext)
    }
}

/**
 * 사용자 컨텍스트와 함께 블록 실행
 */
inline fun <T> withUserContext(
    userId: String,
    block: () -> T
): T {
    val originalUserId = LoggingContext.getUserId()
    return try {
        LoggingContext.setUserContext(userId)
        block()
    } finally {
        originalUserId?.let { LoggingContext.setUserContext(it) } ?: LoggingContext.remove(LoggingContext.USER_ID)
    }
}