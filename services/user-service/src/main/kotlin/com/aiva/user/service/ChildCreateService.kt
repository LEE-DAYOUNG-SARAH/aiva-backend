package com.aiva.user.service

import com.aiva.user.dto.ChildCreateRequest
import com.aiva.user.dto.ChildCreateResponse
import com.aiva.user.entity.BirthType
import com.aiva.user.entity.Child
import com.aiva.user.entity.Gender
import com.aiva.user.repository.ChildRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@Service
@Transactional
class ChildCreateService(
    private val childRepository: ChildRepository
) {
    fun createChild(userIdString: String, request: ChildCreateRequest): ChildCreateResponse {
        val userId = UUID.fromString(userIdString)

        // 중복 자녀 등록 방지
        val hasChild = childRepository.existsByUserId(userId)
        if(hasChild) throw IllegalArgumentException("이미 자녀정보가 존재합니다.")
        
        // 출생일 유효성 검사
        validateBirthDate(request.birthType, request.birthDate)

        val child = childRepository.save(
            Child(
                userId = userId,
                birthType = BirthType.valueOf(request.birthType),
                birthDate = request.birthDate,
                gender = Gender.valueOf(request.gender),
                note = request.note
            )
        )

        return ChildCreateResponse.from(child)
    }
    
    /**
     * 출생일 유효성 검사
     */
    private fun validateBirthDate(birthType: String, birthDate: LocalDate?) {
        // DUE_UNKNOWN이 아닌 경우 출생일 필수
        if (birthType != BirthType.DUE_UNKNOWN.name && birthDate == null) {
            throw IllegalArgumentException("출생 타입이 ${birthType}인 경우 출생일은 필수입니다")
        }
        
        // BORN인 경우 미래 날짜 불허
        if (birthType == BirthType.BORN.name && birthDate?.isAfter(LocalDate.now()) == true) {
            throw IllegalArgumentException("이미 태어난 아이의 출생일은 과거 날짜여야 합니다")
        }
    }
}