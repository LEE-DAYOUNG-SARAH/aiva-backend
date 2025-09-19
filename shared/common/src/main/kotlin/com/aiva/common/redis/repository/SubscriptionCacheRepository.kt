package com.aiva.common.redis.repository

import com.aiva.common.redis.entity.SubscriptionCache
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SubscriptionCacheRepository : CrudRepository<SubscriptionCache, String> {
    
    fun findByUserId(userId: UUID): List<SubscriptionCache>
    
    fun findByStatus(status: SubscriptionCache.SubscriptionStatus): List<SubscriptionCache>
    
    fun findByUserIdAndStatus(userId: UUID, status: SubscriptionCache.SubscriptionStatus): List<SubscriptionCache>
}