package com.aiva.batch

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["com.aiva"])
@EnableBatchProcessing
@EnableScheduling
@EnableFeignClients
@EnableJpaAuditing
class BatchServiceApplication

fun main(args: Array<String>) {
    runApplication<BatchServiceApplication>(*args)
}