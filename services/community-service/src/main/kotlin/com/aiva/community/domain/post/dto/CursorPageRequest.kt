package com.aiva.community.domain.post.dto

import java.time.LocalDateTime
import java.util.*

/**
 * 커서 기반 페이지네이션 요청
 */
data class CursorPageRequest(
    val cursor: String? = null,  // Base64 인코딩된 커서
    val limit: Int = 20
) {
    init {
        require(limit in 1..100) { "Limit must be between 1 and 100" }
    }
}

/**
 * 커서 정보 (createdAt과 postId의 조합)
 */
data class PostCursor(
    val createdAtEpochMs: Long,
    val postId: UUID
) {
    companion object {
        fun fromPost(createdAt: LocalDateTime, postId: UUID): PostCursor {
            return PostCursor(
                createdAtEpochMs = createdAt.atZone(java.time.ZoneOffset.UTC).toInstant().toEpochMilli(),
                postId = postId
            )
        }
        
        fun decode(encodedCursor: String): PostCursor? {
            return try {
                val decoded = String(java.util.Base64.getDecoder().decode(encodedCursor))
                val parts = decoded.split("|")
                if (parts.size == 2) {
                    PostCursor(
                        createdAtEpochMs = parts[0].toLong(),
                        postId = UUID.fromString(parts[1])
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }
    
    fun encode(): String {
        val data = "$createdAtEpochMs|$postId"
        return java.util.Base64.getEncoder().encodeToString(data.toByteArray())
    }
}