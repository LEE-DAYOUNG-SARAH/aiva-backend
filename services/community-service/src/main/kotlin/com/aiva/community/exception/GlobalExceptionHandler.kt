package com.aiva.community.exception

import com.aiva.community.post.dto.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ApiResponse<Unit>> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.message ?: "Invalid argument", "INVALID_ARGUMENT"))
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException): ResponseEntity<ApiResponse<Unit>> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(ex.message ?: "Invalid operation", "INVALID_STATE"))
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ResponseEntity<ApiResponse<Unit>> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("Resource not found", "NOT_FOUND"))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Unit>> {
        val errors = ex.bindingResult.allErrors.joinToString("; ") { error ->
            when (error) {
                is FieldError -> "${error.field}: ${error.defaultMessage}"
                else -> error.defaultMessage ?: "Validation error"
            }
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Validation failed: $errors", "VALIDATION_ERROR"))
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ApiResponse<Unit>> {
        val message = "Invalid value for parameter '${ex.name}': ${ex.value}"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(message, "INVALID_PARAMETER"))
    }

    @ExceptionHandler(SecurityException::class)
    fun handleSecurityException(ex: SecurityException): ResponseEntity<ApiResponse<Unit>> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error(ex.message ?: "Access denied", "ACCESS_DENIED"))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ApiResponse<Unit>> {
        // Log the exception for debugging
        ex.printStackTrace()
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Internal server error", "INTERNAL_ERROR"))
    }
}