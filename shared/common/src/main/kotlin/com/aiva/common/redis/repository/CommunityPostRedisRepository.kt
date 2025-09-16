package com.aiva.common.redis.repository

import com.aiva.common.redis.entity.CommunityPostCache
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CommunityPostRedisRepository : CrudRepository<CommunityPostCache, UUID> {
    
    fun findByAuthorId(authorId: UUID): List<CommunityPostCache>
    
    fun findByAuthorId(authorId: UUID, pageable: Pageable): Page<CommunityPostCache>
    
    fun findAllByOrderByCreatedAtDesc(): List<CommunityPostCache>
    
    fun findAllByOrderByCreatedAtDesc(pageable: Pageable): Page<CommunityPostCache>
    
    fun findAllByOrderByLikeCountDescCreatedAtDesc(): List<CommunityPostCache>
    
    fun findAllByOrderByLikeCountDescCreatedAtDesc(pageable: Pageable): Page<CommunityPostCache>
}