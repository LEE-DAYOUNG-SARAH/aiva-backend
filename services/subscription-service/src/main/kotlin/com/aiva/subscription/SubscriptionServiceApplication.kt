package com.aiva.subscription

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["com.aiva"])
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
class SubscriptionServiceApplication

fun main(args: Array<String>) {
    runApplication<SubscriptionServiceApplication>(*args)
}
