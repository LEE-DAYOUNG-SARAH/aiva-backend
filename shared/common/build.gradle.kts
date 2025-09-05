dependencies {
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework.boot:spring-boot-starter-actuator")
    api("org.springframework.boot:spring-boot-starter-data-redis")
    api("org.springframework.boot:spring-boot-starter-webflux") // WebClient 지원 추가
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    
    // OpenAPI 문서화
    api("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
    
    // 유틸리티
    api("org.apache.commons:commons-lang3:3.13.0")
    api("org.apache.commons:commons-collections4:4.4")
    
}

// 라이브러리 모듈이므로 bootJar 태스크 비활성화
tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
    archiveClassifier = ""
}
