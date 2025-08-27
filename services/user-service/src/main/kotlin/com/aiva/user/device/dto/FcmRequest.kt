package com.aiva.user.device.dto

import com.aiva.user.device.entity.FcmToken
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime
import java.util.*

data class FcmTokenUpdateRequest(
    @field:NotBlank(message = "FCM 토큰은 필수입니다")
    val fcmToken: String
)

data class FcmTokenResponse(
    val id: UUID,
    val fcmToken: String,
    val lastValidatedAt: LocalDateTime,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(fcmToken: FcmToken) = FcmTokenResponse(
            id = fcmToken.id,
            fcmToken = fcmToken.fcmToken,
            lastValidatedAt = fcmToken.lastValidatedAt ?: LocalDateTime.now(),
            createdAt = fcmToken.createdAt
        )
    }
}
