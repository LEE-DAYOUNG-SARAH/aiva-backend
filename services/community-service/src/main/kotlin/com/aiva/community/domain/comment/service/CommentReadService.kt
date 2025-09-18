package com.aiva.community.domain.comment.service

import com.aiva.community.domain.comment.entity.Comment
import com.aiva.community.domain.comment.repository.CommentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class CommentReadService(
    private val commentRepository: CommentRepository
) {
    fun getActiveCommentById(parentCommentId: UUID): Comment {
        return commentRepository.findActiveCommentById(parentCommentId)
            .orElseThrow { IllegalArgumentException("댓글을 찾을 수 없습니다") }
    }
}