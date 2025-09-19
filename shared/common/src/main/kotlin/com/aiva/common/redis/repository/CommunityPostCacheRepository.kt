package com.aiva.common.redis.repository

import com.aiva.common.redis.entity.CommunityPostCache
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CommunityPostCacheRepository : CrudRepository<CommunityPostCache, java.util.UUID> {
    
    fun findByAuthorId(authorId: java.util.UUID): List<CommunityPostCache>
    
    fun findAllByOrderByCreatedAtDesc(): List<CommunityPostCache>
    
    fun findAllByOrderByLikeCountDescCreatedAtDesc(): List<CommunityPostCache>
    
    fun deleteByAuthorId(authorId: java.util.UUID)
}