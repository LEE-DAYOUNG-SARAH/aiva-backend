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
    
    // 결제 관련
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    
    // 스케줄링
    implementation("org.springframework.boot:spring-boot-starter-quartz")
    
    // 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:junit-jupiter")
}
