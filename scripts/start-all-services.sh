#!/bin/bash

echo "🚀 AIVA MSA 전체 서비스를 시작합니다..."

# 인프라 서비스 시작
echo "📦 인프라 서비스 시작..."
docker-compose up -d

# 데이터베이스 준비 대기
echo "⏳ 데이터베이스 준비 대기 (30초)..."
sleep 30

# 서비스별로 순차적으로 시작
services=("user-service" "chat-service" "community-service" "notification-service" "subscription-service")

for service in "${services[@]}"
do
    if [ -d "services/$service" ]; then
        echo "🔄 $service 시작 중..."
        cd "services/$service"
        ./gradlew bootRun &
        cd ../..
        sleep 5
    else
        echo "⚠️  $service 디렉토리가 없습니다. 건너뛰는 중..."
    fi
done

echo "✅ 모든 서비스가 시작되었습니다!"
echo "🔗 서비스 URL:"
echo "   - User Service: http://localhost:8081/swagger-ui.html"
echo "   - Chat Service: http://localhost:8082/swagger-ui.html" 
echo "   - Community Service: http://localhost:8083/swagger-ui.html"
echo "   - Notification Service: http://localhost:8084/swagger-ui.html"
echo "   - Subscription Service: http://localhost:8085/swagger-ui.html"
echo ""
echo "📊 모니터링:"
echo "   - Grafana: http://localhost:3000 (admin/admin)"
echo "   - Prometheus: http://localhost:9090"
echo ""
echo "💾 데이터베이스:"
echo "   - User DB: localhost:3306/aiva_user"
echo "   - Chat DB: localhost:3307/aiva_chat"
echo "   - Community DB: localhost:3308/aiva_community"
echo "   - Notification DB: localhost:3309/aiva_notification"
echo "   - Subscription DB: localhost:3310/aiva_subscription"
echo "   - Redis: localhost:6379"
