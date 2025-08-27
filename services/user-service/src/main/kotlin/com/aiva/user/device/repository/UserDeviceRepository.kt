package com.aiva.user.device.repository

import com.aiva.user.device.entity.UserDevice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserDeviceRepository : JpaRepository<UserDevice, UUID> {
    fun findByUserIdAndDeviceIdentifierAndDeletedAtIsNull(userId: UUID, deviceIdentifier: String): UserDevice?
}
