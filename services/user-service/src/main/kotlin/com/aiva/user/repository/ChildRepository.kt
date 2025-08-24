package com.aiva.user.repository

import com.aiva.user.entity.Child
import com.aiva.user.entity.Gender
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

/**
 * 자녀 정보 Repository
 */
@Repository
interface ChildRepository : JpaRepository<Child, UUID> {
}