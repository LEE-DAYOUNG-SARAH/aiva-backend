package com.aiva.community.domain.post.service

import com.aiva.community.domain.post.dto.UpdatePostImageRequest
import com.aiva.community.domain.post.entity.CommunityPostImage
import com.aiva.community.domain.post.repository.CommunityPostImageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class CommunityPostImageService(
    private val communityPostImageRepository: CommunityPostImageRepository
) {

    @Transactional
    fun createAll(postId: UUID, imageUrls: List<String>) {
        val images = imageUrls.map {
            CommunityPostImage(
                postId = postId,
                url = it
            )
        }

        communityPostImageRepository.saveAll(images)
    }

    @Transactional
    fun update(postId: UUID, images: List<UpdatePostImageRequest>) {
        images.forEach { request ->
            when {
                request.isDeleted && request.imageId != null -> {
                    // Delete existing image
                    communityPostImageRepository.deleteById(request.imageId)
                }
                !request.isDeleted && request.imageId != null -> {
                    // Update existing image (if needed)
                    val existingImage = communityPostImageRepository.findById(request.imageId)
                    if (existingImage.isPresent && existingImage.get().url != request.s3Url) {
                        val updatedImage = existingImage.get().copy(
                            url = request.s3Url,
                            updatedAt = LocalDateTime.now()
                        )
                        communityPostImageRepository.save(updatedImage)
                    }
                }
                !request.isDeleted && request.imageId == null -> {
                    // Add new image
                    val newImage = CommunityPostImage(
                        postId = postId,
                        url = request.s3Url
                    )
                    communityPostImageRepository.save(newImage)
                }
            }
        }
    }
}