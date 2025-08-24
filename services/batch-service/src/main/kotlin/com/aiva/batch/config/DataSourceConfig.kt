package com.aiva.batch.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
class DataSourceConfig {

    // Batch 메타데이터용 H2 DB (Primary)
    @Primary
    @Bean(name = "batchDataSource")
    @ConfigurationProperties("spring.datasource.batch")
    fun batchDataSource(): DataSource {
        return DataSourceBuilder.create().build()
    }

    // User Service DB (읽기 전용)
    @Bean(name = "userDataSource")
    @ConfigurationProperties("spring.datasource.user")
    fun userDataSource(): DataSource {
        return DataSourceBuilder.create().build()
    }

    // Subscription Service DB (읽기 전용)
    @Bean(name = "subscriptionDataSource")
    @ConfigurationProperties("spring.datasource.subscription")
    fun subscriptionDataSource(): DataSource {
        return DataSourceBuilder.create().build()
    }

    // Notification Service DB (읽기 전용)
    @Bean(name = "notificationDataSource")
    @ConfigurationProperties("spring.datasource.notification")
    fun notificationDataSource(): DataSource {
        return DataSourceBuilder.create().build()
    }
}

// User 엔티티용 JPA 설정
@Configuration
@EnableJpaRepositories(
    basePackages = ["com.aiva.batch.repository.user"],
    entityManagerFactoryRef = "userEntityManagerFactory",
    transactionManagerRef = "userTransactionManager"
)
class UserDataSourceConfig {

    @Bean(name = "userEntityManagerFactory")
    fun userEntityManagerFactory(@Qualifier("userDataSource") dataSource: DataSource): LocalContainerEntityManagerFactoryBean {
        val em = LocalContainerEntityManagerFactoryBean()
        em.dataSource = dataSource
        em.setPackagesToScan("com.aiva.batch.entity.user")
        em.jpaVendorAdapter = HibernateJpaVendorAdapter()
        return em
    }

    @Bean(name = "userTransactionManager")
    fun userTransactionManager(@Qualifier("userEntityManagerFactory") userEntityManagerFactory: LocalContainerEntityManagerFactoryBean): PlatformTransactionManager {
        val transactionManager = JpaTransactionManager()
        transactionManager.entityManagerFactory = userEntityManagerFactory.getObject()
        return transactionManager
    }
}

// Subscription 엔티티용 JPA 설정
@Configuration
@EnableJpaRepositories(
    basePackages = ["com.aiva.batch.repository.subscription"],
    entityManagerFactoryRef = "subscriptionEntityManagerFactory",
    transactionManagerRef = "subscriptionTransactionManager"
)
class SubscriptionDataSourceConfig {

    @Bean(name = "subscriptionEntityManagerFactory")
    fun subscriptionEntityManagerFactory(@Qualifier("subscriptionDataSource") dataSource: DataSource): LocalContainerEntityManagerFactoryBean {
        val em = LocalContainerEntityManagerFactoryBean()
        em.dataSource = dataSource
        em.setPackagesToScan("com.aiva.batch.entity.subscription")
        em.jpaVendorAdapter = HibernateJpaVendorAdapter()
        return em
    }

    @Bean(name = "subscriptionTransactionManager")
    fun subscriptionTransactionManager(@Qualifier("subscriptionEntityManagerFactory") subscriptionEntityManagerFactory: LocalContainerEntityManagerFactoryBean): PlatformTransactionManager {
        val transactionManager = JpaTransactionManager()
        transactionManager.entityManagerFactory = subscriptionEntityManagerFactory.getObject()
        return transactionManager
    }
}

// Notification 엔티티용 JPA 설정
@Configuration
@EnableJpaRepositories(
    basePackages = ["com.aiva.batch.repository.notification"],
    entityManagerFactoryRef = "notificationEntityManagerFactory",
    transactionManagerRef = "notificationTransactionManager"
)
class NotificationDataSourceConfig {

    @Bean(name = "notificationEntityManagerFactory")
    fun notificationEntityManagerFactory(@Qualifier("notificationDataSource") dataSource: DataSource): LocalContainerEntityManagerFactoryBean {
        val em = LocalContainerEntityManagerFactoryBean()
        em.dataSource = dataSource
        em.setPackagesToScan("com.aiva.batch.entity.notification")
        em.jpaVendorAdapter = HibernateJpaVendorAdapter()
        return em
    }

    @Bean(name = "notificationTransactionManager")
    fun notificationTransactionManager(@Qualifier("notificationEntityManagerFactory") notificationEntityManagerFactory: LocalContainerEntityManagerFactoryBean): PlatformTransactionManager {
        val transactionManager = JpaTransactionManager()
        transactionManager.entityManagerFactory = notificationEntityManagerFactory.getObject()
        return transactionManager
    }
}