#!/bin/bash

if [ $# -eq 0 ]; then
    echo "사용법: ./create-service.sh <service-name>"
    echo "예시: ./create-service.sh payment-service"
    exit 1
fi

SERVICE_NAME=$1
SERVICE_DIR="services/$SERVICE_NAME"

echo "🚀 새 서비스 '$SERVICE_NAME' 생성 중..."

# 디렉토리 구조 생성
mkdir -p "$SERVICE_DIR/src/main/kotlin/com/aiva/${SERVICE_NAME//-/_}/{entity,repository,service,controller,dto}"
mkdir -p "$SERVICE_DIR/src/main/resources/db/migration"
mkdir -p "$SERVICE_DIR/src/test/kotlin/com/aiva/${SERVICE_NAME//-/_}"

# build.gradle.kts 생성
cat > "$SERVICE_DIR/build.gradle.kts" << EOL
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
    
    // 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:junit-jupiter")
}
EOL

# 메인 애플리케이션 클래스 생성
PACKAGE_NAME=${SERVICE_NAME//-/_}
CLASS_NAME=$(echo ${SERVICE_NAME//-/ } | sed 's/\b\w/\U&/g' | sed 's/ //g')

cat > "$SERVICE_DIR/src/main/kotlin/com/aiva/$PACKAGE_NAME/${CLASS_NAME}Application.kt" << EOL
package com.aiva.$PACKAGE_NAME

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication(scanBasePackages = ["com.aiva"])
@EnableJpaAuditing
class ${CLASS_NAME}Application

fun main(args: Array<String>) {
    runApplication<${CLASS_NAME}Application>(*args)
}
EOL

echo "✅ 서비스 '$SERVICE_NAME' 생성 완료!"
echo "📁 디렉토리: $SERVICE_DIR"
echo "🔧 settings.gradle.kts에 다음 줄을 추가해주세요:"
echo "    \"services:$SERVICE_NAME\","
