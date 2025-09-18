package com.aiva.chat.dto.user

import java.util.*

data class UserInfo(
    val id: UUID,
    val isBorn: Boolean,
    val childBirthdate: String?,
    val gender: String // FEMALE/MALE/UNKNOWN
)