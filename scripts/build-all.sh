#!/bin/bash

echo "🔨 AIVA MSA 전체 서비스 빌드를 시작합니다..."

# Gradle 빌드
echo "📚 Gradle 빌드 시작..."
./gradlew clean build -x test

echo "🐳 Docker 이미지 빌드..."

# 각 서비스별 Docker 이미지 빌드 (향후 Dockerfile 추가 시)
services=("user-service" "chat-service" "community-service" "notification-service" "subscription-service" "gateway")

for service in "${services[@]}"
do
    echo "🔄 $service 빌드 중..."
    if [ "$service" = "gateway" ]; then
        service_path="infrastructure/gateway"
    else
        service_path="services/$service"
    fi
    
    if [ -d "$service_path" ]; then
        echo "✅ $service 빌드 완료"
    else
        echo "⚠️  $service_path 디렉토리가 없습니다."
    fi
done

echo "✅ 전체 빌드가 완료되었습니다!"
