package com.aiva.user.device.dto

import jakarta.validation.constraints.Size

data class DeviceUpdateRequest(
    @field:Size(max = 100, message = "디바이스 모델은 100자를 초과할 수 없습니다")
    val deviceModel: String? = null,
    
    @field:Size(max = 50, message = "OS 버전은 50자를 초과할 수 없습니다")
    val osVersion: String? = null
)

data class DeviceResponse(
    val id: String,
    val deviceIdentifier: String,
    val platform: String,
    val deviceModel: String?,
    val osVersion: String?,
    val appVersion: String,
    val lastSeenAt: String,
    val createdAt: String
)
