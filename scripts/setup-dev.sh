#!/bin/bash

echo "🚀 AIVA MSA 개발 환경 설정을 시작합니다..."

# Docker Compose로 인프라 서비스 시작
echo "📦 Docker Compose로 인프라 서비스를 시작합니다..."
docker-compose up -d mysql-user mysql-chat mysql-community mysql-notification mysql-subscription redis

# 데이터베이스 준비 대기
echo "⏳ 데이터베이스 준비를 기다립니다..."
sleep 30

# Gradle 의존성 다운로드 및 컴파일
echo "�� Gradle 의존성을 다운로드하고 컴파일합니다..."
./gradlew build -x test

echo "✅ 개발 환경 설정이 완료되었습니다!"
echo "🔗 다음 URL에서 서비스를 확인할 수 있습니다:"
echo "   - User Service: http://localhost:8081/swagger-ui.html"
echo "   - Chat Service: http://localhost:8082/swagger-ui.html"
echo "   - Community Service: http://localhost:8083/swagger-ui.html"
echo "   - Notification Service: http://localhost:8084/swagger-ui.html"
echo "   - Subscription Service: http://localhost:8085/swagger-ui.html"
echo "   - Grafana: http://localhost:3000 (admin/admin)"
echo "   - Prometheus: http://localhost:9090"
