package com.aiva.user.service

import com.aiva.user.dto.ChildRequest
import com.aiva.user.dto.ChildResponse
import com.aiva.user.entity.BirthType
import com.aiva.user.entity.Gender
import com.aiva.user.repository.ChildRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class ChildUpdateService(
    private val childRepository: ChildRepository
) {

    fun updateChild(userId: UUID, request: ChildRequest):ChildResponse {
        val child = childRepository.findByUserId(userId)
            ?: throw IllegalArgumentException("자녀 정보가 존재하지 않습니다.")

        ChildValidationUtils.validateBirthDate(request.birthType, request.birthDate)

        child.update(
            birthType = BirthType.valueOf(request.birthType),
            birthDate = request.birthDate,
            gender = Gender.valueOf(request.gender),
            note = request.note
        )

        return ChildResponse.from(child)
    }
}