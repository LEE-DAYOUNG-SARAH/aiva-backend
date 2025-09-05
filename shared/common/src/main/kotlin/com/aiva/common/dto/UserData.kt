package com.aiva.common.dto

import java.util.*

data class UserData(
    val userId: UUID,
    val email: String,
    val nickname: String,
    val avatarUrl: String?
)
