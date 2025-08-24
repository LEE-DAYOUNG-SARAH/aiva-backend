package com.aiva.batch.job

import com.aiva.batch.entity.notification.BatchNotification
import com.aiva.batch.repository.notification.BatchNotificationRepository
import com.aiva.batch.client.NotificationServiceClient
import com.aiva.batch.client.SendScheduledNotificationRequest
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
 * 예약 알림 발송 배치 Job 설정
 * - 예약된 시간이 된 알림들을 찾아서 발송 처리
 * - 대상 사용자들에게 실제 알림 발송
 */
@Configuration
class ScheduledNotificationJobConfig(
    private val jobRepository: JobRepository,
    private val notificationTransactionManager: PlatformTransactionManager,
    private val batchNotificationRepository: BatchNotificationRepository,
    private val notificationServiceClient: NotificationServiceClient
) {

    @Bean
    fun scheduledNotificationJob(): Job {
        return JobBuilder("scheduledNotificationJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(scheduledNotificationStep())
            .build()
    }

    @Bean
    fun scheduledNotificationStep(): Step {
        return StepBuilder("scheduledNotificationStep", jobRepository)
            .chunk<BatchNotification, ScheduledNotificationResult>(5, notificationTransactionManager)
            .reader(scheduledNotificationReader())
            .processor(scheduledNotificationProcessor())
            .writer(scheduledNotificationWriter())
            .build()
    }

    @Bean
    fun scheduledNotificationReader(): RepositoryItemReader<BatchNotification> {
        return RepositoryItemReaderBuilder<BatchNotification>()
            .name("scheduledNotificationReader")
            .repository(batchNotificationRepository)
            .methodName("findScheduledNotificationsToSend")
            .arguments(LocalDateTime.now())
            .sorts(mapOf("scheduledAt" to Sort.Direction.ASC))
            .build()
    }

    @Bean
    fun scheduledNotificationProcessor(): ItemProcessor<BatchNotification, ScheduledNotificationResult> {
        return ItemProcessor { notification ->
            try {
                // TODO: 예약 알림 발송 준비 로직 구현
                // 1. 대상 사용자 목록 조회
                // 2. 알림 설정 확인
                // 3. 발송 가능한 사용자 필터링
                
                ScheduledNotificationResult(
                    notification = notification,
                    success = true,
                    targetUserCount = 0, // TODO: 실제 대상 사용자 수 계산
                    message = "예약 알림 발송 준비 완료"
                )
            } catch (e: Exception) {
                ScheduledNotificationResult(
                    notification = notification,
                    success = false,
                    targetUserCount = 0,
                    message = "예약 알림 처리 실패: ${e.message}"
                )
            }
        }
    }

    @Bean
    fun scheduledNotificationWriter(): ItemWriter<ScheduledNotificationResult> {
        return ItemWriter { results ->
            results.forEach { result ->
                try {
                    if (result.success) {
                        // TODO: 실제 알림 발송 API 호출
                        println("예약 알림 발송 완료: ${result.notification.id} - 대상자 ${result.targetUserCount}명")
                    } else {
                        println("예약 알림 발송 실패: ${result.notification.id} - ${result.message}")
                    }
                } catch (e: Exception) {
                    println("예약 알림 후처리 실패: ${result.notification.id} - ${e.message}")
                }
            }
        }
    }
}

data class ScheduledNotificationResult(
    val notification: BatchNotification,
    val success: Boolean,
    val targetUserCount: Int,
    val message: String
)