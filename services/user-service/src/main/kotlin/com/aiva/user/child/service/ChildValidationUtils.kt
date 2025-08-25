package com.aiva.user.child.service

import com.aiva.user.child.entity.BirthType
import java.time.LocalDate

class ChildValidationUtils {

    companion object {
        /**
         * 출생일 유효성 검사
         */
        fun validateBirthDate(birthType: String, birthDate:LocalDate?) {
            // DUE_UNKNOWN이 아닌 경우 출생일 필수
            if (birthType != BirthType.DUE_UNKNOWN.name && birthDate == null) {
                throw IllegalArgumentException("출생 타입이 ${birthType}인 경우 출생일은 필수입니다")
            }

            // BORN인 경우 미래 날짜 불허
            if (birthType == BirthType.BORN.name && birthDate?.isAfter(LocalDate.now()) == true) {
                throw IllegalArgumentException("이미 태어난 아이의 출생일은 과거 날짜여야 합니다")
            }

            // DUE인 경우 과거 날짜 불허
            if (birthType == BirthType.DUE.name && birthDate?.isBefore(LocalDate.now()) == true) {
                throw IllegalArgumentException("예정일은 미래 날짜여야 합니다")
            }
        }
    }
}