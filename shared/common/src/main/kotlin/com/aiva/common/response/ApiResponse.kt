package com.aiva.common.response

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorResponse? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(success = true, data = data)
        }
        
        fun success(): ApiResponse<Unit> {
            return ApiResponse(success = true)
        }
        
        fun <T> error(error: ErrorResponse): ApiResponse<T> {
            return ApiResponse(success = false, error = error)
        }
        
        fun <T> error(message: String, code: String = "INTERNAL_ERROR"): ApiResponse<T> {
            return ApiResponse(success = false, error = ErrorResponse(code, message))
        }
    }
}

data class ErrorResponse(
    val code: String,
    val message: String,
    val details: Map<String, Any>? = null
)
