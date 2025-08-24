package com.aiva.batch.job

import com.aiva.batch.entity.subscription.BatchUserSubscription
import com.aiva.batch.repository.subscription.BatchSubscriptionRepository
import com.aiva.batch.client.SubscriptionServiceClient
import com.aiva.batch.client.NotificationServiceClient
import com.aiva.batch.client.ExpireSubscriptionRequest
import com.aiva.batch.client.SubscriptionExpiredNotificationRequest
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
import java.time.format.DateTimeFormatter

/**
 * 구독 만료 처리 배치 Job 설정
 * - 만료된 구독들을 찾아서 상태를 EXPIRED로 변경
 * - 사용자에게 만료 알림 발송
 */
@Configuration
class SubscriptionExpiryJobConfig(
    private val jobRepository: JobRepository,
    private val subscriptionTransactionManager: PlatformTransactionManager,
    private val batchSubscriptionRepository: BatchSubscriptionRepository,
    private val subscriptionServiceClient: SubscriptionServiceClient,
    private val notificationServiceClient: NotificationServiceClient
) {

    @Bean
    fun subscriptionExpiryJob(): Job {
        return JobBuilder("subscriptionExpiryJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(subscriptionExpiryStep())
            .build()
    }

    @Bean
    fun subscriptionExpiryStep(): Step {
        return StepBuilder("subscriptionExpiryStep", jobRepository)
            .chunk<BatchUserSubscription, SubscriptionExpiryResult>(10, subscriptionTransactionManager)
            .reader(expiredSubscriptionReader())
            .processor(subscriptionExpiryProcessor())
            .writer(subscriptionExpiryWriter())
            .build()
    }

    @Bean
    fun expiredSubscriptionReader(): RepositoryItemReader<BatchUserSubscription> {
        return RepositoryItemReaderBuilder<BatchUserSubscription>()
            .name("expiredSubscriptionReader")
            .repository(batchSubscriptionRepository)
            .methodName("findExpiredActiveSubscriptions")
            .arguments(LocalDateTime.now())
            .sorts(mapOf("currentPeriodEnd" to Sort.Direction.ASC))
            .build()
    }

    @Bean
    fun subscriptionExpiryProcessor(): ItemProcessor<BatchUserSubscription, SubscriptionExpiryResult> {
        return ItemProcessor { subscription ->
            try {
                // TODO: 구독 만료 처리 로직 구현
                // 1. 구독 상태를 EXPIRED로 변경
                // 2. 사용자 Pro 권한 해제
                // 3. 만료 알림 발송 준비
                
                SubscriptionExpiryResult(
                    subscription = subscription,
                    success = true,
                    message = "구독 만료 처리 완료"
                )
            } catch (e: Exception) {
                SubscriptionExpiryResult(
                    subscription = subscription,
                    success = false,
                    message = "구독 만료 처리 실패: ${e.message}"
                )
            }
        }
    }

    @Bean
    fun subscriptionExpiryWriter(): ItemWriter<SubscriptionExpiryResult> {
        return ItemWriter { results ->
            results.forEach { result ->
                try {
                    if (result.success) {
                        // TODO: 실제 API 호출 로직 구현
                        // 1. 구독 서비스 API 호출 - 구독 만료 처리
                        // 2. 알림 서비스 API 호출 - 만료 알림 발송
                        
                        println("구독 만료 처리 성공: ${result.subscription.id}")
                    } else {
                        println("구독 만료 처리 실패: ${result.subscription.id} - ${result.message}")
                    }
                } catch (e: Exception) {
                    println("구독 만료 후처리 실패: ${result.subscription.id} - ${e.message}")
                }
            }
        }
    }
}

data class SubscriptionExpiryResult(
    val subscription: BatchUserSubscription,
    val success: Boolean,
    val message: String
)