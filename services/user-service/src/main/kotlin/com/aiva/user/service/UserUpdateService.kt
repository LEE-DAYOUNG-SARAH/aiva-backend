package com.aiva.user.service

import com.aiva.user.dto.UserAvatarUpdateRequest
import com.aiva.user.dto.UserAvatarUpdateResponse
import com.aiva.user.repository.UserRepository
import com.aiva.security.exception.UnauthorizedException
import com.aiva.user.dto.UserInfoUpdateRequest
import com.aiva.user.dto.UserInfoUpdateResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class UserUpdateService(
    private val childUpdateService: ChildUpdateService,
    private val userRepository: UserRepository
) {
    fun uploadAvatarImage(userIdString: String, request: UserAvatarUpdateRequest): UserAvatarUpdateResponse {
        val userId = UUID.fromString(userIdString)
        val user = userRepository.findByIdAndDeletedAtIsNull(userId)
            ?: throw UnauthorizedException("사용자를 찾을 수 없습니다")
        
        user.uploadImage(request.avatarUrl)
        
        return UserAvatarUpdateResponse(avatarUrl = request.avatarUrl)
    }

    fun deleteAvatarImage(userIdString: String) {
        val userId = UUID.fromString(userIdString)
        val user = userRepository.findByIdAndDeletedAtIsNull(userId)
            ?: throw UnauthorizedException("사용자를 찾을 수 없습니다")

        user.deleteImage()
    }

    // 내 정보 수정 (닉네임 + 자녀정보)
    fun updateUserInfo(userIdString: String, request: UserInfoUpdateRequest): UserInfoUpdateResponse {
        val userId = UUID.fromString(userIdString)
        val user = userRepository.findByIdAndDeletedAtIsNull(userId)
            ?: throw UnauthorizedException("사용자를 찾을 수 없습니다")

        // 닉네임 수정
        user.updateNickname(request.nickname)

        // 자녀정보 수정
        val childResponse = childUpdateService.updateChild(userId, request.childInfo)

        return UserInfoUpdateResponse(
            nickname = user.nickname,
            childInfo = childResponse
        )
    }
}
