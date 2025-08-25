package com.aiva.user.dto

import com.aiva.user.entity.BirthType
import com.aiva.user.entity.Child
import com.aiva.user.entity.Gender
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.util.*

// 자녀 정보 생성/수정 동시 사용
data class ChildRequest(
    @field:NotBlank(message = "출생 타입은 필수입니다")
    @field:Pattern(
        regexp = "^(BORN|DUE|DUE_UNKNOWN)$",
        message = "출생 타입은 BORN, DUE, DUE_UNKNOWN 중 하나여야 합니다"
    )
    val birthType: String,
    
    val birthDate: LocalDate?,
    
    @field:NotBlank(message = "성별은 필수입니다")
    @field:Pattern(
        regexp = "^(FEMALE|MALE|UNKNOWN)$",
        message = "성별은 FEMALE, MALE, UNKNOWN 중 하나여야 합니다"
    )
    val gender: String,
    
    @field:Size(max = 500, message = "메모는 500자를 초과할 수 없습니다")
    val note: String? = null
)

data class ChildResponse(
    val id: UUID,
    val birthType: BirthType,
    val birthDate: LocalDate?,
    val gender: Gender,
    val note: String?
) {
    companion object {
        fun from(child: Child) = ChildResponse(
            id = child.id,
            birthType = child.birthType,
            birthDate = child.birthDate,
            gender = child.gender,
            note = child.note
        )
    }
}