package com.aiva.batch.scheduler

import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 배치 작업 스케줄러
 * Phase 1 필수 배치 작업들의 스케줄링 담당
 */
@Component
class BatchScheduler(
    private val jobLauncher: JobLauncher,
    @Qualifier("subscriptionExpiryJob") private val subscriptionExpiryJob: Job,
    @Qualifier("subscriptionRenewalJob") private val subscriptionRenewalJob: Job,
    @Qualifier("scheduledNotificationJob") private val scheduledNotificationJob: Job
) {

    /**
     * 구독 만료 처리 배치 (매일 새벽 1시)
     * - 만료된 구독들을 EXPIRED 상태로 변경
     * - 사용자 Pro 권한 해제
     * - 만료 알림 발송
     */
    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
    fun runSubscriptionExpiryJob() {
        try {
            val jobParameters = createJobParameters("subscriptionExpiry")
            jobLauncher.run(subscriptionExpiryJob, jobParameters)
            println("구독 만료 처리 배치 실행 완료: ${LocalDateTime.now()}")
        } catch (e: Exception) {
            println("구독 만료 처리 배치 실행 실패: ${e.message}")
        }
    }

    /**
     * 구독 자동 갱신 처리 배치 (매일 새벽 2시)
     * - 자동 갱신이 필요한 구독들의 결제 처리
     * - 갱신 성공/실패에 따른 구독 상태 업데이트
     * - 갱신 실패 시 알림 발송
     */
    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul")
    fun runSubscriptionRenewalJob() {
        try {
            val jobParameters = createJobParameters("subscriptionRenewal")
            jobLauncher.run(subscriptionRenewalJob, jobParameters)
            println("구독 자동 갱신 배치 실행 완료: ${LocalDateTime.now()}")
        } catch (e: Exception) {
            println("구독 자동 갱신 배치 실행 실패: ${e.message}")
        }
    }

    /**
     * 예약 알림 발송 배치 (매 10분마다)
     * - 예약된 시간이 된 알림들을 대상 사용자에게 발송
     * - 사용자 알림 설정에 따른 필터링
     * - 대량 발송 처리
     */
    @Scheduled(cron = "0 */10 * * * *", zone = "Asia/Seoul")
    fun runScheduledNotificationJob() {
        try {
            val jobParameters = createJobParameters("scheduledNotification")
            jobLauncher.run(scheduledNotificationJob, jobParameters)
            println("예약 알림 발송 배치 실행 완료: ${LocalDateTime.now()}")
        } catch (e: Exception) {
            println("예약 알림 발송 배치 실행 실패: ${e.message}")
        }
    }

    /**
     * 배치 작업용 고유 파라미터 생성
     */
    private fun createJobParameters(jobName: String): JobParameters {
        return JobParametersBuilder()
            .addString("jobName", jobName)
            .addLong("timestamp", System.currentTimeMillis())
            .addString("executionTime", LocalDateTime.now().toString())
            .toJobParameters()
    }
}