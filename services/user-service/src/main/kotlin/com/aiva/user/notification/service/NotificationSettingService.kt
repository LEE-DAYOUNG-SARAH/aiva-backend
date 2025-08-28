package com.aiva.user.notification.service

import com.aiva.security.exception.UnauthorizedException
import com.aiva.user.notification.dto.NotificationPermissionResponse
import com.aiva.user.notification.dto.NotificationSettingUpdateRequest
import com.aiva.user.notification.dto.NotificationSettingUpdateResponse
import com.aiva.user.notification.entity.*
import com.aiva.user.notification.repository.NotificationConsentEventRepository
import com.aiva.user.notification.repository.UserNotificationSettingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class NotificationSettingService(
    private val userNotificationSettingRepository: UserNotificationSettingRepository,
    private val consentEventRepository: NotificationConsentEventRepository
) {
    
    companion object {
        // TODO. 테이블 관리 필요
        private const val POLICY_VERSION = "1.0"
    }


    fun createNotificationSetting(userId: UUID, systemNotificationEnabled: Boolean) {
        val newSettings = NotificationPermissionType.entries.map {
            UserNotificationSetting(
                userId = userId,
                permissionType = it,
                isEnabled = if(systemNotificationEnabled) it.defaultValue else false
            )
        }
        userNotificationSettingRepository.saveAll(newSettings)

        val newConsents = NotificationPermissionType.entries.map {
            NotificationConsentEvent(
                userId = userId,
                permissionType = it,
                action = if(systemNotificationEnabled) ConsentAction.OPT_IN else ConsentAction.OPT_OUT,
                source = ConsentSource.SYSTEM,
                policyVersion = POLICY_VERSION
            )
        }
        consentEventRepository.saveAll(newConsents)
    }

    @Transactional(readOnly = true)
    fun getNotificationSettings(userId: UUID): List<NotificationPermissionResponse> {
        return userNotificationSettingRepository.findByUserId(userId).map {
            NotificationPermissionResponse.from(it)
        }
    }

    fun updateNotificationSetting(
        userId: UUID,
        settingId: UUID,
        request: NotificationSettingUpdateRequest
    ): NotificationSettingUpdateResponse {
        val setting = userNotificationSettingRepository.findByIdAndUserId(settingId, userId)
            ?: throw UnauthorizedException("사용자 알림 정보를 찾을 수 없습니다.")

        if(setting.isEnabled != request.isEnabled) {
            setting.updateEnabled(request.isEnabled)

            consentEventRepository.save(
                NotificationConsentEvent(
                    userId = userId,
                    permissionType = setting.permissionType,
                    action = if(request.isEnabled) ConsentAction.OPT_IN else ConsentAction.OPT_OUT,
                    source = ConsentSource.MYPAGE_TOGGLE,
                    policyVersion = POLICY_VERSION
                )
            )
        }

        return NotificationSettingUpdateResponse.from(setting)
    }

}
