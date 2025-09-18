package com.aiva.notification.exception

import com.aiva.common.response.ApiResponse
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.format.DateTimeParseException

@RestControllerAdvice
class NotificationExceptionHandler {
    
    private val logger = KotlinLogging.logger {}
    
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Unit>> {
        val errors = e.bindingResult.allErrors.map { error ->
            when (error) {
                is FieldError -> "${error.field}: ${error.defaultMessage}"
                else -> error.defaultMessage ?: "유효하지 않은 요청입니다"
            }
        }
        
        logger.warn { "Validation error: ${errors.joinToString(", ")}" }
        
        return ResponseEntity.badRequest().body(
            ApiResponse.error(
                message = "요청 데이터가 유효하지 않습니다: ${errors.joinToString(", ")}",
                code = "VALIDATION_ERROR"
            )
        )
    }
    
    @ExceptionHandler(DateTimeParseException::class)
    fun handleDateTimeParseException(e: DateTimeParseException): ResponseEntity<ApiResponse<Unit>> {
        logger.warn { "Invalid date format: ${e.message}" }
        
        return ResponseEntity.badRequest().body(
            ApiResponse.error(
                message = "날짜 형식이 올바르지 않습니다. ISO 8601 형식(yyyy-MM-ddTHH:mm:ss)을 사용해주세요.",
                code = "INVALID_DATE_FORMAT"
            )
        )
    }
    
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ApiResponse<Unit>> {
        logger.warn { "Invalid argument: ${e.message}" }
        
        return ResponseEntity.badRequest().body(
            ApiResponse.error(
                message = e.message ?: "잘못된 요청 매개변수입니다.",
                code = "INVALID_ARGUMENT"
            )
        )
    }
    
    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<ApiResponse<Unit>> {
        logger.error(e) { "Unexpected error occurred: ${e.message}" }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiResponse.error(
                message = "서버 내부 오류가 발생했습니다.",
                code = "INTERNAL_SERVER_ERROR"
            )
        )
    }
}