package com.aiva.community.post.repository

import com.aiva.community.post.entity.CommunityPostImage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CommunityPostImageRepository : JpaRepository<CommunityPostImage, UUID> {

    fun findByPostIdOrderByCreatedAtAsc(postId: UUID): List<CommunityPostImage>

    @Modifying
    @Query("DELETE FROM CommunityPostImage cpi WHERE cpi.postId = :postId")
    fun deleteByPostId(@Param("postId") postId: UUID)

    @Query("SELECT COUNT(cpi) FROM CommunityPostImage cpi WHERE cpi.postId = :postId")
    fun countByPostId(@Param("postId") postId: UUID): Long
}