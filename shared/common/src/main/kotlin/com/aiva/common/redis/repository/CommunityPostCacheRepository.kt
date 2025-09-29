package com.aiva.common.redis.repository

import com.aiva.common.redis.entity.CommunityPostCache
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Redis Hash에 저장된 커뮤니티 게시물 캐시를 위한 Repository
 * SortedSet과 함께 사용하여 최신글 목록 관리
 */
@Repository
interface CommunityPostCacheRepository : CrudRepository<CommunityPostCache, UUID> {
    
    /**
     * 작성자별 게시물 조회
     */
    fun findByAuthorId(authorId: UUID): List<CommunityPostCache>
    
    /**
     * 작성자별 게시물 삭제
     */
    fun deleteByAuthorId(authorId: UUID)
}