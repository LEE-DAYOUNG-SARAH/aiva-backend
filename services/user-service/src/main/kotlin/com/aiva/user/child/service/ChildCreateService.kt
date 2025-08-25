package com.aiva.user.child.service

import com.aiva.user.child.dto.ChildRequest
import com.aiva.user.child.dto.ChildResponse
import com.aiva.user.child.entity.BirthType
import com.aiva.user.child.entity.Child
import com.aiva.user.child.entity.Gender
import com.aiva.user.child.repository.ChildRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class ChildCreateService(
    private val childRepository: ChildRepository
) {
    fun createChild(userIdString: String, request: ChildRequest): ChildResponse {
        val userId = UUID.fromString(userIdString)

        // 중복 자녀 등록 방지
        val hasChild = childRepository.existsByUserId(userId)
        if(hasChild) throw IllegalArgumentException("이미 자녀정보가 존재합니다.")
        
        // 출생일 유효성 검사
        ChildValidationUtils.validateBirthDate(request.birthType, request.birthDate)


        val child = childRepository.save(
            Child(
                userId = userId,
                birthType = BirthType.valueOf(request.birthType),
                birthDate = request.birthDate,
                gender = Gender.valueOf(request.gender),
                note = request.note
            )
        )

        return ChildResponse.from(child)
    }
}