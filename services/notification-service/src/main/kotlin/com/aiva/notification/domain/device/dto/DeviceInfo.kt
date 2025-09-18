package com.aiva.notification.domain.device.dto

data class DeviceInfo(
    val deviceIdentifier: String,   // 디바이스 고유 식별자
    val platform: String,          // "ANDROID", "IOS", "WEB"  
    val appVersion: String          // 앱 버전
)