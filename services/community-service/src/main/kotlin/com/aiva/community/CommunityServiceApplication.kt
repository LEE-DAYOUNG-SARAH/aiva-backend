package com.aiva.community

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication(scanBasePackages = ["com.aiva"])
@EnableJpaAuditing
class CommunityServiceApplication

fun main(args: Array<String>) {
    runApplication<CommunityServiceApplication>(*args)
}
