package com.aiva.common.redis.repository

import com.aiva.common.redis.entity.ChildCache
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ChildRedisRepository : CrudRepository<ChildCache, UUID> {
    
    /**
     * userId로 자녀 정보 조회
     */
    fun findByUserId(userId: UUID): ChildCache?
}
