package com.aiva.notification.domain.notification.dto

data class ReadAllRequest(
    val beforeDate: String? = null // ISO 8601 format (optional)
)

data class ReadAllResponse(
    val readCount: Int
)