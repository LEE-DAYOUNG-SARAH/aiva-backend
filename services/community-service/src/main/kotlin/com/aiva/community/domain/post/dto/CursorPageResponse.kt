package com.aiva.community.domain.post.dto

/**
 * 커서 기반 페이지네이션 응답
 */
data class CursorPageResponse<T>(
    val content: List<T>,
    val nextCursor: String?,  // 다음 페이지를 위한 커서 (마지막 페이지면 null)
    val hasNext: Boolean,     // 다음 페이지 존재 여부
    val size: Int             // 현재 페이지 크기
) {
    companion object {
        fun <T> of(content: List<T>, nextCursor: String?, hasNext: Boolean): CursorPageResponse<T> {
            return CursorPageResponse(
                content = content,
                nextCursor = nextCursor,
                hasNext = hasNext,
                size = content.size
            )
        }
    }
}