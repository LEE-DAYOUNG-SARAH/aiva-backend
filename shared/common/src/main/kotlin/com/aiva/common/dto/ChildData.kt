package com.aiva.common.dto

import java.time.LocalDate
import java.util.*

data class ChildData(
    val childId: UUID,
    val isBorn: Boolean,
    val childBirthdate: LocalDate?,
    val gender: String // FEMALE/MALE/UNKNOWN
)
