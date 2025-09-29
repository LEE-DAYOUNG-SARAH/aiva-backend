package com.aiva.community.domain.post.service

import com.aiva.common.response.PageResponse
import com.aiva.community.domain.post.dto.*
import com.aiva.community.domain.post.entity.CommunityPost
import com.aiva.community.domain.post.repository.CommunityPostRepository
import com.aiva.community.domain.post.repository.CommunityPostImageRepository
import com.aiva.common.redis.service.RedisCommunityServiceV2
import com.aiva.community.global.cache.toCommunityPost
import com.aiva.community.global.cache.toCommunityPostCache
import com.aiva.community.domain.user.UserGrpcClient
import java.time.LocalDateTime
import java.time.ZoneOffset
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class CommunityPostReadService(
    private val communityPostRepository: CommunityPostRepository,
    private val communityPostImageRepository: CommunityPostImageRepository,
    private val redisCommunityServiceV2: RedisCommunityServiceV2,
    private val userGrpcClient: UserGrpcClient
) {
    
    private val logger = KotlinLogging.logger {}

    fun getActivePostById(postId: UUID): CommunityPost {
        // Redis Hash에서 우선 조회
        val cachedPost = redisCommunityServiceV2.getPost(postId)
        if (cachedPost != null) {
            logger.debug { "Cache hit for post: $postId" }
            return cachedPost.toCommunityPost()
        }
        
        // 캐시 미스 시 DB에서 조회
        val post = communityPostRepository.findActivePostById(postId)
            .orElseThrow { IllegalArgumentException("Post not found or deleted: $postId") }
            
        // 조회된 게시물을 Redis Hash에 저장
        val postCache = post.toCommunityPostCache()
        redisCommunityServiceV2.savePost(postCache)
        
        logger.debug { "Retrieved post from DB and cached: $postId" }
        
        return post
    }
    
    /**
     * 단일 게시물 조회 (gRPC로 사용자 정보 포함)
     */
    fun getActivePostWithAuthor(postId: UUID): CommunityPostWithAuthor {
        val post = getActivePostById(postId)
        
        // gRPC로 사용자 정보 조회
        val author = userGrpcClient.getUserProfile(post.userId)
            ?: userGrpcClient.createFallbackAuthorInfo(post.userId)
        
        val postWithAuthor = CommunityPostWithAuthor(
            post = post,
            imageUrls = post.images.map { it.url },
            author = author
        )
        
        return postWithAuthor
    }

    /**
     * 최신 게시물 목록 조회 (SortedSet + Hash 구조)
     */
    fun getActivePosts(pageable: Pageable): Page<CommunityPost> {
        val pageNumber = pageable.pageNumber + 1 // 0-based to 1-based
        val pageSize = pageable.pageSize
        
        // SortedSet에서 게시물 ID 목록 조회
        val postIds = redisCommunityServiceV2.getLatestPosts(pageNumber, pageSize)
        
        if (postIds.isNotEmpty()) {
            // Hash에서 게시물 상세 정보 조회
            val posts = postIds.mapNotNull { postId ->
                redisCommunityServiceV2.getPost(postId)?.toCommunityPost()
            }
            
            if (posts.isNotEmpty()) {
                val totalElements = redisCommunityServiceV2.getTotalPostCount()
                logger.debug { "Cache hit for post list page: $pageNumber, found ${posts.size} posts" }
                return PageImpl(posts, pageable, totalElements)
            }
        }
        
        // 캐시 미스 시 DB에서 조회하고 캐시에 저장
        return getFromDbAndWarmCache(pageable)
    }

    private fun getFromDbAndWarmCache(pageable: Pageable): Page<CommunityPost> {
        logger.debug { "Cache miss for post list, querying DB" }

        // DB에서 조회
        val dbResult = communityPostRepository.findActivePosts(pageable)

        // 각 게시물을 Redis에 저장 (Hash + SortedSet)
        dbResult.content.forEach { post ->
            val postCache = post.toCommunityPostCache()
            redisCommunityServiceV2.savePost(postCache)
        }

        logger.debug { "Cached ${dbResult.content.size} posts to Redis" }

        return dbResult
    }
    
    /**
     * 최신 게시물 목록 조회 (gRPC로 사용자 정보 포함)
     */
    fun getActivePostsWithAuthors(pageable: Pageable): Page<CommunityPostWithAuthor> {
        val postsPage = getActivePosts(pageable)
        
        if (postsPage.content.isEmpty()) {
            return PageImpl(emptyList(), pageable, 0)
        }
        
        // 사용자 ID들 추출하여 gRPC 배치 조회
        val userIds = postsPage.content.map { it.userId }.distinct()
        val userProfiles = userGrpcClient.getUserProfiles(userIds)
        
        logger.debug { "Retrieved ${userProfiles.size} user profiles via gRPC for ${postsPage.content.size} posts" }
        
        val postsWithAuthors = postsPage.content.map { post ->
            val author = userProfiles[post.userId] 
                ?: userGrpcClient.createFallbackAuthorInfo(post.userId)
            
            CommunityPostWithAuthor(
                post = post,
                imageUrls = post.images.map { it.url },
                author = author
            )
        }
        
        return PageImpl(postsWithAuthors, pageable, postsPage.totalElements)
    }

    fun getPopularPosts(pageable: Pageable): Page<CommunityPost> {
        // 인기 게시물은 DB에서 조회 (SortedSet은 최신순 전용)
        val result = communityPostRepository.findPopularPosts(pageable)
        
        // 개별 게시물을 Redis에 저장
        result.content.forEach { post ->
            val postCache = post.toCommunityPostCache()
            redisCommunityServiceV2.savePost(postCache)
        }
        
        return result
    }
    
    /**
     * 인기 게시물 목록 조회 (gRPC로 사용자 정보 포함)
     */
    fun getPopularPostsWithAuthors(pageable: Pageable): Page<CommunityPostWithAuthor> {
        val postsPage = getPopularPosts(pageable)
        
        if (postsPage.content.isEmpty()) {
            return PageImpl(emptyList(), pageable, 0)
        }
        
        // 사용자 ID들 추출하여 gRPC 배치 조회
        val userIds = postsPage.content.map { it.userId }.distinct()
        val userProfiles = userGrpcClient.getUserProfiles(userIds)
        
        val postsWithAuthors = postsPage.content.map { post ->
            val author = userProfiles[post.userId] 
                ?: userGrpcClient.createFallbackAuthorInfo(post.userId)
            
            CommunityPostWithAuthor(
                post = post,
                imageUrls = post.images.map { it.url },
                author = author
            )
        }
        
        return PageImpl(postsWithAuthors, pageable, postsPage.totalElements)
    }
    
    /**
     * 특정 사용자의 게시물 목록 조회 (gRPC로 사용자 정보 포함)
     */
    fun getUserPostsWithAuthor(userId: UUID, pageable: Pageable): Page<CommunityPostWithAuthor> {
        val postsPage = communityPostRepository.findActivePostsByUserId(userId, pageable)
        
        if (postsPage.content.isEmpty()) {
            return PageImpl(emptyList(), pageable, 0)
        }
        
        // 동일 사용자이므로 한 번만 조회
        val userProfile = userGrpcClient.getUserProfile(userId)
            ?: userGrpcClient.createFallbackAuthorInfo(userId)
        
        val postsWithAuthors = postsPage.content.map { post ->
            CommunityPostWithAuthor(
                post = post,
                imageUrls = post.images.map { it.url },
                author = userProfile
            )
        }
        
        return PageImpl(postsWithAuthors, pageable, postsPage.totalElements)
    }

    fun getUserPostCount(userId: UUID): Long {
        return communityPostRepository.countActivePostsByUserId(userId)
    }
    
    /**
     * 커서 기반 최신 게시물 목록 조회 (사용자 정보 포함)
     */
    fun getActivePostsWithAuthorsByCursor(request: CursorPageRequest): CursorPageResponse<CommunityPostWithAuthor> {
        val cutoffTime = LocalDateTime.now().minusHours(24)
        
        val cursor = request.cursor?.let { PostCursor.decode(it) }
        val limit = request.limit
        
        val posts = mutableListOf<CommunityPost>()
        
        // 1. 캐시에서 조회 (24시간 이내)
        val lastScore = cursor?.createdAtEpochMs?.toDouble()?.div(1000) // milliseconds to seconds
        val cachePostIds = redisCommunityServiceV2.getLatestPostsByCursor(lastScore, limit + 1) // +1로 hasNext 확인
        
        // 커서 기반 필터링 (동일한 timestamp인 경우 postId로 구분)
        val filteredCachePostIds = if (cursor != null) {
            cachePostIds.filter { postId ->
                val post = redisCommunityServiceV2.getPost(postId)
                if (post != null) {
                    val postCreatedAtMs = post.createdAt.toEpochSecond(ZoneOffset.UTC) * 1000
                    when {
                        postCreatedAtMs < cursor.createdAtEpochMs -> true
                        postCreatedAtMs == cursor.createdAtEpochMs -> postId.toString() < cursor.postId.toString()
                        else -> false
                    }
                } else false
            }
        } else {
            cachePostIds
        }
        
        // 캐시에서 게시물 상세 정보 조회
        val cachePosts = filteredCachePostIds.take(limit).mapNotNull { postId ->
            redisCommunityServiceV2.getPost(postId)?.toCommunityPost()
        }.filter { it.createdAt >= cutoffTime } // 24시간 컷오프 적용
        
        posts.addAll(cachePosts)
        
        // 2. 캐시에서 부족한 경우 DB에서 추가 조회
        val remainingLimit = limit - posts.size
        if (remainingLimit > 0) {
            val dbPosts = getPostsFromDbWithCursor(cursor, cutoffTime, remainingLimit)
            posts.addAll(dbPosts)
        }
        
        // 3. 사용자 정보 조회 (캐시 우선)
        val finalPosts = posts.take(limit)
        val userIds = finalPosts.map { it.userId }.distinct()
        val userProfiles = userGrpcClient.getUserProfiles(userIds)
        
        val postsWithAuthors = finalPosts.map { post ->
            val author = userProfiles[post.userId] 
                ?: userGrpcClient.createFallbackAuthorInfo(post.userId)
            
            CommunityPostWithAuthor(
                post = post,
                imageUrls = post.images.map { it.url },
                author = author
            )
        }
        
        // 4. 다음 커서 생성
        val hasNext = posts.size > limit || cachePostIds.size > limit
        val nextCursor = if (hasNext && postsWithAuthors.isNotEmpty()) {
            val lastPost = postsWithAuthors.last().post
            PostCursor.fromPost(lastPost.createdAt, lastPost.id).encode()
        } else null
        
        logger.debug { "Retrieved ${postsWithAuthors.size} posts with cursor (${cachePosts.size} from cache, ${posts.size - cachePosts.size} from DB)" }
        
        return CursorPageResponse.of(postsWithAuthors, nextCursor, hasNext)
    }
    
    /**
     * DB에서 커서 기반 게시물 조회 (24시간 이전 데이터용)
     */
    private fun getPostsFromDbWithCursor(cursor: PostCursor?, cutoffTime: LocalDateTime, limit: Int): List<CommunityPost> {
        return try {
            val posts = if (cursor != null) {
                val cursorCreatedAt = LocalDateTime.ofEpochSecond(cursor.createdAtEpochMs / 1000, 0, ZoneOffset.UTC)
                communityPostRepository.findActivePostsWithKeyset(cursorCreatedAt, cursor.postId, limit)
            } else {
                communityPostRepository.findActivePostsFromTime(cutoffTime, limit)
            }
            
            // 조회된 게시물들을 캐시에 저장
            posts.forEach { post ->
                val postCache = post.toCommunityPostCache()
                redisCommunityServiceV2.savePost(postCache)
            }
            
            logger.debug { "Retrieved ${posts.size} posts from DB for cursor pagination" }
            posts
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to get posts from DB with cursor" }
            emptyList()
        }
    }
}