package com.aiva.batch.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * Subscription Service API 클라이언트 (하이브리드 방식의 API 호출용)
 */
@FeignClient(
    name = "subscription-service",
    url = "\${api.subscription-service.url:http://localhost:8085}"
)
interface SubscriptionServiceClient {
    
    /**
     * 구독 상태 업데이트
     */
    @PutMapping("/api/subscriptions/{subscriptionId}/status")
    fun updateSubscriptionStatus(
        @PathVariable subscriptionId: UUID,
        @RequestBody request: UpdateSubscriptionStatusRequest
    ): SubscriptionApiResponse
    
    /**
     * 구독 자동 갱신 처리
     */
    @PostMapping("/api/subscriptions/{subscriptionId}/renew")
    fun renewSubscription(
        @PathVariable subscriptionId: UUID,
        @RequestBody request: RenewSubscriptionRequest
    ): SubscriptionApiResponse
    
    /**
     * 구독 만료 처리
     */
    @PostMapping("/api/subscriptions/{subscriptionId}/expire")
    fun expireSubscription(
        @PathVariable subscriptionId: UUID,
        @RequestBody request: ExpireSubscriptionRequest
    ): SubscriptionApiResponse
    
    /**
     * 구독 정보 조회
     */
    @GetMapping("/api/subscriptions/{subscriptionId}")
    fun getSubscription(@PathVariable subscriptionId: UUID): SubscriptionDetailResponse
}

data class UpdateSubscriptionStatusRequest(
    val status: String,
    val reason: String? = null
)

data class RenewSubscriptionRequest(
    val paymentMethodId: String? = null,
    val automaticRenewal: Boolean = true
)

data class ExpireSubscriptionRequest(
    val reason: String? = null,
    val notifyUser: Boolean = true
)

data class SubscriptionApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: Any? = null
)

data class SubscriptionDetailResponse(
    val id: UUID,
    val userId: UUID,
    val planId: UUID,
    val status: String,
    val currentPeriodEnd: String,
    val nextBillingAt: String?
)