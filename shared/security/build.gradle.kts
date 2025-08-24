dependencies {
    api(project(":shared:common"))
    
    // Spring Security & JWT
    api("org.springframework.boot:spring-boot-starter-security")
    api("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")
    
    // Web & Validation (ArgumentResolver 등을 위해)
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-validation")
    
    // Redis (JWT 블랙리스트 등)
    api("org.springframework.boot:spring-boot-starter-data-redis")
}

// 라이브러리 모듈이므로 bootJar 태스크 비활성화
tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
    archiveClassifier = ""
}