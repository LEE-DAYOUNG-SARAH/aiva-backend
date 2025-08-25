package com.aiva.user.child.controller

import com.aiva.common.response.ApiResponse
import com.aiva.user.child.dto.ChildRequest
import com.aiva.user.child.service.ChildCreateService
import com.aiva.user.child.dto.ChildResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/children")
class ChildCreateController(
    private val childCreateService: ChildCreateService
) {
    @PostMapping
    fun createChild(
        @RequestHeader("X-User-Id") userId: String,
        @Valid @RequestBody request: ChildRequest
    ): ApiResponse<ChildResponse> = ApiResponse.success(
        childCreateService.createChild(userId, request)
    )
}