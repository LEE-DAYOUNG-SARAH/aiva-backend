package com.aiva.user.child.controller

import com.aiva.common.dto.ChildData
import com.aiva.common.response.ApiResponse
import com.aiva.user.child.service.ChildReadService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/children")
class ChildReadController(
    private val childReadService: ChildReadService
) {
    @GetMapping("/{userId}")
    fun getChildData(
        @PathVariable userId: String
    ): ApiResponse<ChildData?> = ApiResponse.success(
        childReadService.getChildData(UUID.fromString(userId))
    )
}