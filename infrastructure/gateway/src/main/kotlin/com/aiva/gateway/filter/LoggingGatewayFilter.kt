package com.aiva.gateway.filter

import mu.KotlinLogging
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.*

/**
 * API Gateway에서 모든 요청에 대해 상관관계 ID를 생성하고 전파하는 필터
 * 
 * 이 필터는 가장 먼저 실행되어:
 * 1. Correlation ID가 없으면 새로 생성
 * 2. Request ID 생성
 * 3. 하위 서비스로 헤더 전파
 * 4. 응답에 추적 정보 포함
 */
@Component
class LoggingGatewayFilter : GlobalFilter, Ordered {
    
    private val logger = KotlinLogging.logger {}
    
    companion object {
        const val X_CORRELATION_ID = "X-Correlation-ID"
        const val X_REQUEST_ID = "X-Request-ID"
        const val X_TRACE_ID = "X-Trace-ID"
        const val X_SERVICE_NAME = "X-Service-Name"
        const val GATEWAY_SERVICE_NAME = "api-gateway"
    }
    
    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val response = exchange.response
        
        val startTime = System.currentTimeMillis()
        
        // Correlation ID 추출 또는 생성
        val correlationId = request.headers.getFirst(X_CORRELATION_ID) 
            ?: UUID.randomUUID().toString()
        
        // Request ID 추출 또는 생성
        val requestId = request.headers.getFirst(X_REQUEST_ID) 
            ?: UUID.randomUUID().toString()
        
        // Trace ID 생성 (분산 추적용)
        val traceId = UUID.randomUUID().toString()
        
        // 요청 로깅
        logRequest(request, correlationId, requestId, traceId)
        
        // 하위 서비스로 전달할 헤더 추가
        val modifiedRequest = request.mutate()
            .header(X_CORRELATION_ID, correlationId)
            .header(X_REQUEST_ID, requestId)
            .header(X_TRACE_ID, traceId)
            .header(X_SERVICE_NAME, GATEWAY_SERVICE_NAME)
            .build()
        
        // 응답 헤더에 추적 정보 추가
        response.headers.add(X_CORRELATION_ID, correlationId)
        response.headers.add(X_REQUEST_ID, requestId)
        response.headers.add(X_TRACE_ID, traceId)
        
        val modifiedExchange = exchange.mutate()
            .request(modifiedRequest)
            .build()
        
        return chain.filter(modifiedExchange)
            .doOnSuccess { 
                logResponse(modifiedRequest, response, correlationId, requestId, startTime, null)
            }
            .doOnError { error ->
                logResponse(modifiedRequest, response, correlationId, requestId, startTime, error)
            }
    }
    
    private fun logRequest(
        request: ServerHttpRequest,
        correlationId: String,
        requestId: String,
        traceId: String
    ) {
        val queryParams = if (request.queryParams.isNotEmpty()) {
            request.queryParams.toSingleValueMap().toString()
        } else {
            null
        }
        
        logger.info {
            buildString {
                append("Gateway Request Started - ")
                append("Method: ${request.method.name()}, ")
                append("URI: ${request.uri.path}, ")
                append("CorrelationId: $correlationId, ")
                append("RequestId: $requestId, ")
                append("TraceId: $traceId, ")
                append("RemoteAddress: ${request.remoteAddress?.address?.hostAddress}")
                if (queryParams != null) {
                    append(", QueryParams: $queryParams")
                }
            }
        }
    }
    
    private fun logResponse(
        request: ServerHttpRequest,
        response: org.springframework.http.server.reactive.ServerHttpResponse,
        correlationId: String,
        requestId: String,
        startTime: Long,
        error: Throwable?
    ) {
        val duration = System.currentTimeMillis() - startTime
        val statusCode = response.statusCode?.value() ?: 0
        
        val logMessage = buildString {
            append("Gateway Request Completed - ")
            append("Method: ${request.method.name()}, ")
            append("URI: ${request.uri.path}, ")
            append("Status: $statusCode, ")
            append("Duration: ${duration}ms, ")
            append("CorrelationId: $correlationId, ")
            append("RequestId: $requestId")
            
            if (error != null) {
                append(", Error: ${error.javaClass.simpleName}: ${error.message}")
            }
        }
        
        when {
            error != null -> logger.error(error) { logMessage }
            statusCode >= 500 -> logger.error { logMessage }
            statusCode >= 400 -> logger.warn { logMessage }
            else -> logger.info { logMessage }
        }
        
        // 성능 메트릭 로깅 (느린 요청 감지)
        if (duration > 5000) { // 5초 이상
            logger.warn { 
                "Slow Request Detected - URI: ${request.uri.path}, " +
                "Duration: ${duration}ms, CorrelationId: $correlationId"
            }
        }
    }
    
    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE
}