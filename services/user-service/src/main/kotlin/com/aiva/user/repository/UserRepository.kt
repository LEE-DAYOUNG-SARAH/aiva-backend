package com.aiva.user.repository

import com.aiva.user.entity.Provider
import com.aiva.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

/**
 * 사용자 Repository
 */
@Repository
interface UserRepository : JpaRepository<User, UUID> {
    
    /**
     * OAuth 제공자와 제공자 사용자 ID로 사용자 조회
     */
    fun findByProviderAndProviderUserId(provider: Provider, providerUserId: String): User?
}