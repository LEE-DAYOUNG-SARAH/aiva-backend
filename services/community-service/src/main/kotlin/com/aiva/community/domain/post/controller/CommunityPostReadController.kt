package com.aiva.community.domain.post.controller

import com.aiva.common.response.ApiResponse
import com.aiva.common.response.PageResponse
import com.aiva.community.domain.post.dto.*
import com.aiva.community.domain.post.service.CommunityPostReadService
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/api/v1/posts")
class CommunityPostReadController(
    private val communityPostReadService: CommunityPostReadService
) {

    @GetMapping
    fun getCommunityFeed(
        pageable: Pageable,
        principal: Principal
    ): ApiResponse<PageResponse<CommunityPostResponse>> {
        val postsWithAuthors = communityPostReadService.getActivePostsWithAuthors(pageable)
        
        val postResponses = postsWithAuthors.content.map { postWithAuthor ->
            postWithAuthor.toCommunityPostResponse()
        }
        
        val response = PageResponse(
            content = postResponses,
            totalElements = postsWithAuthors.totalElements,
            totalPages = postsWithAuthors.totalPages,
            currentPage = postsWithAuthors.number
        )

        return ApiResponse.success(response)
    }

    @GetMapping("/{postId}")
    fun getPost(
        @PathVariable postId: UUID,
        principal: Principal
    ): ApiResponse<CommunityPostResponse> {
        // 로컬 프로젝션을 통한 단일 게시물 + 사용자 정보 조회
        val postWithAuthor = communityPostReadService.getActivePostWithAuthor(postId)
        val response = postWithAuthor.toCommunityPostResponse()

        return ApiResponse.success(response)
    }

    @GetMapping("/me")
    fun getUserPosts(
        pageable: Pageable,
        principal: Principal
    ): ApiResponse<PageResponse<CommunityPostResponse>> {
        val userId = UUID.fromString(principal.name)
        
        // 로컬 프로젝션을 통한 사용자 게시물 + 사용자 정보 조회
        val postsWithAuthors = communityPostReadService.getUserPostsWithAuthor(userId, pageable)
        
        val postResponses = postsWithAuthors.content.map { postWithAuthor ->
            postWithAuthor.toCommunityPostResponse()
        }
        
        val response = PageResponse(
            content = postResponses,
            totalElements = postsWithAuthors.totalElements,
            totalPages = postsWithAuthors.totalPages,
            currentPage = postsWithAuthors.number
        )
        
        return ApiResponse.success(response)
    }
    
    @GetMapping("/popular")
    fun getPopularPosts(
        pageable: Pageable,
        principal: Principal
    ): ApiResponse<PageResponse<CommunityPostResponse>> {
        // 로컬 프로젝션을 통한 인기 게시물 + 사용자 정보 조회
        val postsWithAuthors = communityPostReadService.getPopularPostsWithAuthors(pageable)
        
        val postResponses = postsWithAuthors.content.map { postWithAuthor ->
            postWithAuthor.toCommunityPostResponse()
        }
        
        val response = PageResponse(
            content = postResponses,
            totalElements = postsWithAuthors.totalElements,
            totalPages = postsWithAuthors.totalPages,
            currentPage = postsWithAuthors.number
        )
        
        return ApiResponse.success(response)
    }
    
    /**
     * 커서 기반 최신 게시물 목록 조회
     */
    @GetMapping("/cursor")
    fun getCommunityFeedWithCursor(
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "20") limit: Int,
        principal: Principal
    ): ApiResponse<CursorPageResponse<CommunityPostResponse>> {
        val request = CursorPageRequest(cursor, limit)
        val postsWithAuthors = communityPostReadService.getActivePostsWithAuthorsByCursor(request)
        
        val postResponses = postsWithAuthors.content.map { postWithAuthor ->
            postWithAuthor.toCommunityPostResponse()
        }
        
        val response = CursorPageResponse.of(
            content = postResponses,
            nextCursor = postsWithAuthors.nextCursor,
            hasNext = postsWithAuthors.hasNext
        )
        
        return ApiResponse.success(response)
    }
}