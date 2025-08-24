dependencies {
    implementation(project(":shared:common"))
    implementation(project(":shared:security"))
    
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    
    // Database
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")
    
    // AWS SDK
    implementation("software.amazon.awssdk:sns:2.21.1")
    implementation("software.amazon.awssdk:sqs:2.21.1")
    
    // FCM
    implementation("com.google.firebase:firebase-admin:9.2.0")
    
    // 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:junit-jupiter")
}
