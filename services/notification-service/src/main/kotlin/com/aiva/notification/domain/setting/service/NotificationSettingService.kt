package com.aiva.notification.domain.setting.service

import com.aiva.notification.domain.setting.dto.NotificationPermissionResponse
import com.aiva.notification.domain.setting.dto.NotificationSettingUpdateRequest
import com.aiva.notification.domain.setting.dto.NotificationSettingUpdateResponse
import com.aiva.notification.domain.setting.entity.*
import com.aiva.notification.domain.setting.repository.NotificationConsentEventRepository
import com.aiva.notification.domain.setting.repository.UserNotificationSettingRepository
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
            ?: throw IllegalArgumentException("사용자 알림 정보를 찾을 수 없습니다.")

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
    
    /**
     * 알림 발송 전 사용자 권한 확인용 메서드
     */
    @Transactional(readOnly = true)
    fun filterUsersWithEnabledNotification(
        userIds: List<UUID>, 
        permissionType: NotificationPermissionType
    ): List<UUID> {
        return userNotificationSettingRepository.findEnabledUserIdsByUserIdsAndPermissionType(userIds, permissionType)
    }
    
    /**
     * 특정 사용자의 특정 권한 활성화 여부 확인
     */
    @Transactional(readOnly = true)
    fun isNotificationEnabled(userId: UUID, permissionType: NotificationPermissionType): Boolean {
        return userNotificationSettingRepository.findByUserIdAndPermissionType(userId, permissionType)
            ?.isEnabled ?: false
    }
}