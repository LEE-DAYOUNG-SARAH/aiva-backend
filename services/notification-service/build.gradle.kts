dependencies {
    implementation(project(":shared:common"))
    implementation(project(":shared:security"))
    
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    
    // Database
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")
    
    // Kafka
    implementation("org.springframework.kafka:spring-kafka")
    
    
    // FCM
    implementation("com.google.firebase:firebase-admin:9.2.0")
    
    // 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}
