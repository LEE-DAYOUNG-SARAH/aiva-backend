package com.aiva.user.repository

import com.aiva.user.entity.Provider
import com.aiva.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * 사용자 Repository
 */
@Repository
interface UserRepository : JpaRepository<User, UUID> {
    
    /**
     * OAuth 제공자와 제공자 사용자 ID로 사용자 조회
     */
    fun findByProviderAndProviderUserIdAndDeletedAtIsNull(provider: Provider, providerUserId: String): User?

    /**
     * 회원 조회
     */
    fun findByIdAndDeletedAtIsNull(userId: UUID): User?
}