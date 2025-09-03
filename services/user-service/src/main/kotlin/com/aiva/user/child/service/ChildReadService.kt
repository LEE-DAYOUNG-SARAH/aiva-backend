package com.aiva.user.child.service

import com.aiva.common.dto.ChildData
import com.aiva.user.child.entity.BirthType
import com.aiva.user.child.repository.ChildRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 자녀 조회 관련 비즈니스 로직 서비스
 * 자녀 정보 조회, 존재 여부 확인 등을 담당
 */
@Service
@Transactional(readOnly = true)
class ChildReadService(
    private val childRepository: ChildRepository
) {
    
    /**
     * 사용자에게 자녀가 있는지 확인
     */
    fun hasChild(userId: UUID): Boolean {
        return childRepository.existsByUserId(userId)
    }

    fun getChildData(userId: UUID): ChildData? {
        return childRepository.findByUserId(userId)?.let {
            ChildData(
                childId = it.id,
                isBorn = it.birthType == BirthType.BORN,
                childBirthdate = it.birthDate,
                gender = it.gender.name
            )
        }
    }
}
