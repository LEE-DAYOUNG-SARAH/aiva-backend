package com.aiva.user.repository

import com.aiva.user.entity.Child
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * 자녀 정보 Repository
 */
@Repository
interface ChildRepository : JpaRepository<Child, UUID> {
    
    /**
     * 특정 사용자의 자녀가 있는지 확인
     */
    fun existsByUserId(userId: UUID): Boolean
}