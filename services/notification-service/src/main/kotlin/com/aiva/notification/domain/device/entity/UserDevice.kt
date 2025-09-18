package com.aiva.notification.device.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "user_devices")
@EntityListeners(AuditingEntityListener::class)
data class UserDevice(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "user_id", nullable = false)
    val userId: UUID,
    
    @Column(name = "device_identifier", nullable = false, length = 255)
    val deviceIdentifier: String,
    
    @Column(name = "platform", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    val platform: Platform,
    
    @Column(name = "device_model", length = 100)
    var deviceModel: String? = null,
    
    @Column(name = "os_version", length = 50)
    var osVersion: String? = null,
    
    @Column(name = "app_version", nullable = false, length = 50)
    var appVersion: String,
    
    @Column(name = "last_seen_at", nullable = false)
    var lastSeenAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null,
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun updateLastSeen() {
        this.lastSeenAt = LocalDateTime.now()
    }
    
    /**
     * 로그인 시 앱 버전 업데이트
     */
    fun updateAppVersion(appVersion: String) {
        this.appVersion = appVersion
        updateLastSeen()
    }
    
    /**
     * 디바이스 하드웨어 정보 업데이트 (사용자 설정)
     */
    fun updateDeviceDetails(deviceModel: String?, osVersion: String?) {
        this.deviceModel = deviceModel
        this.osVersion = osVersion
        updateLastSeen()
    }
}

enum class Platform {
    ANDROID, IOS, WEB
}