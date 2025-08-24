package com.aiva.batch.job

import com.aiva.batch.entity.subscription.BatchUserSubscription
import com.aiva.batch.repository.subscription.BatchSubscriptionRepository
import com.aiva.batch.client.SubscriptionServiceClient
import com.aiva.batch.client.NotificationServiceClient
import com.aiva.batch.client.RenewSubscriptionRequest
import com.aiva.batch.client.SubscriptionRenewalFailedNotificationRequest
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.data.RepositoryItemReader
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Sort
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDateTime

/**
 * 구독 자동 갱신 처리 배치 Job 설정
 * - 자동 갱신이 필요한 구독들을 찾아서 갱신 처리
 * - 갱신 실패 시 사용자에게 알림 발송
 */
@Configuration
class SubscriptionRenewalJobConfig(
    private val jobRepository: JobRepository,
    private val subscriptionTransactionManager: PlatformTransactionManager,
    private val batchSubscriptionRepository: BatchSubscriptionRepository,
    private val subscriptionServiceClient: SubscriptionServiceClient,
    private val notificationServiceClient: NotificationServiceClient
) {

    @Bean
    fun subscriptionRenewalJob(): Job {
        return JobBuilder("subscriptionRenewalJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(subscriptionRenewalStep())
            .build()
    }

    @Bean
    fun subscriptionRenewalStep(): Step {
        return StepBuilder("subscriptionRenewalStep", jobRepository)
            .chunk<BatchUserSubscription, SubscriptionRenewalResult>(10, subscriptionTransactionManager)
            .reader(renewalRequiredSubscriptionReader())
            .processor(subscriptionRenewalProcessor())
            .writer(subscriptionRenewalWriter())
            .build()
    }

    @Bean
    fun renewalRequiredSubscriptionReader(): RepositoryItemReader<BatchUserSubscription> {
        return RepositoryItemReaderBuilder<BatchUserSubscription>()
            .name("renewalRequiredSubscriptionReader")
            .repository(batchSubscriptionRepository)
            .methodName("findSubscriptionsForAutoRenewal")
            .arguments(LocalDateTime.now())
            .sorts(mapOf("nextBillingAt" to Sort.Direction.ASC))
            .build()
    }

    @Bean
    fun subscriptionRenewalProcessor(): ItemProcessor<BatchUserSubscription, SubscriptionRenewalResult> {
        return ItemProcessor { subscription ->
            try {
                // TODO: 구독 자동 갱신 처리 로직 구현
                // 1. 결제 정보 확인
                // 2. 자동 결제 시도
                // 3. 결과에 따른 구독 상태 업데이트
                
                SubscriptionRenewalResult(
                    subscription = subscription,
                    success = true,
                    renewalSuccess = true,
                    message = "구독 자동 갱신 완료"
                )
            } catch (e: Exception) {
                SubscriptionRenewalResult(
                    subscription = subscription,
                    success = false,
                    renewalSuccess = false,
                    message = "구독 자동 갱신 실패: ${e.message}"
                )
            }
        }
    }

    @Bean
    fun subscriptionRenewalWriter(): ItemWriter<SubscriptionRenewalResult> {
        return ItemWriter { results ->
            results.forEach { result ->
                try {
                    if (result.success) {
                        if (result.renewalSuccess) {
                            // TODO: 갱신 성공 처리
                            println("구독 자동 갱신 성공: ${result.subscription.id}")
                        } else {
                            // TODO: 갱신 실패 알림 발송
                            println("구독 자동 갱신 실패 알림 발송: ${result.subscription.id}")
                        }
                    } else {
                        println("구독 자동 갱신 처리 실패: ${result.subscription.id} - ${result.message}")
                    }
                } catch (e: Exception) {
                    println("구독 갱신 후처리 실패: ${result.subscription.id} - ${e.message}")
                }
            }
        }
    }
}

data class SubscriptionRenewalResult(
    val subscription: BatchUserSubscription,
    val success: Boolean,
    val renewalSuccess: Boolean,
    val message: String
)