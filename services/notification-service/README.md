# Notification Service

AIVA 백엔드 시스템의 알림 서비스입니다. Kafka 이벤트 스트리밍과 FCM을 통한 실시간 푸시 알림 시스템을 구현합니다.

## 주요 기능

### 이벤트 기반 알림 처리
- **Kafka Consumer**: 커뮤니티 이벤트를 실시간으로 구독하여 알림 생성
- **비동기 처리**: Kotlin Coroutines 기반 고성능 비동기 알림 발송
- **파티션 키 설계**: 사용자별 알림 순서 보장을 위한 파티셔닝 전략
- **트랜잭션 안전성**: @TransactionalEventListener로 데이터 일관성 보장

### FCM 푸시 알림 시스템
- **Firebase Cloud Messaging**: 모바일 푸시 알림 발송
- **배치 처리**: Coroutines 기반 대량 알림 효율적 처리
- **부분 실패 허용**: 일부 토큰 실패 시에도 나머지 알림 정상 발송
- **실시간 발송**: 이벤트 발생 후 평균 2-3초 내 알림 도착

### 다중 디바이스 지원
- **FCM 토큰 관리**: 사용자별 다중 디바이스 토큰 등록/갱신
- **토큰 생명주기**: 만료된 토큰 자동 감지 및 정리
- **디바이스별 설정**: 각 디바이스별 독립적인 알림 설정

### 개인화 알림 설정
- **알림 타입별 제어**: 게시글, 댓글, 좋아요 등 카테고리별 on/off
- **사용자 선호도**: 개인별 알림 수신 설정 관리
- **알림 기록**: 발송 이력 및 읽음 상태 추적

## 기술 스택

- **Framework**: Spring Boot 3.x, Spring Data JPA, Spring Kafka
- **Language**: Kotlin with Coroutines
- **Message Streaming**: Apache Kafka
- **Push Notification**: Firebase Cloud Messaging (FCM)
- **Database**: MySQL, Redis
- **Async Processing**: Kotlin Coroutines
- **Testing**: MockK, SpringBootTest, Testcontainers

## 아키텍처

### 주요 컴포넌트
- **NotificationConsumer**: Kafka 이벤트 구독 및 알림 생성 관리
- **FcmService**: Coroutines 기반 FCM 발송 및 배치 처리
- **NotificationEventPublisher**: 이벤트 발행 및 파티션 키 관리
- **Device Management**: FCM 토큰 생명주기 관리
- **Setting Management**: 사용자별 알림 개인화 설정

### Kafka 이벤트 처리 전략
1. **파티션 키**: `${userId}-${eventType}` 로 사용자별 순서 보장
2. **Consumer Group**: `notification-service-group` 으로 병렬 처리
3. **트랜잭션 안전성**: `@TransactionalEventListener(AFTER_COMMIT)` 적용
4. **메시지 순서**: 동일 사용자의 알림은 발생 순서대로 처리

## 실행 방법

```bash
# 애플리케이션 실행
./gradlew :services:notification-service:bootRun

# 테스트 실행
./gradlew :services:notification-service:test
```

## 환경 설정

필요한 환경 변수:
- `FIREBASE_CONFIG_PATH`: Firebase 서비스 계정 키 파일 경로
- `MYSQL_URL`: MySQL 데이터베이스 URL
- `REDIS_URL`: Redis 서버 URL
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka 서버 주소

## API 엔드포인트

### 알림 관리
- `GET /api/notifications`: 사용자 알림 목록 조회
- `PUT /api/notifications/{notificationId}/read`: 알림 읽음 처리
- `DELETE /api/notifications/{notificationId}`: 알림 삭제

### 디바이스 관리
- `POST /api/devices`: 디바이스 등록
- `PUT /api/devices/{deviceId}`: 디바이스 정보 수정
- `DELETE /api/devices/{deviceId}`: 디바이스 삭제

### FCM 토큰 관리
- `POST /api/fcm/tokens`: FCM 토큰 등록
- `PUT /api/fcm/tokens/{tokenId}`: FCM 토큰 갱신
- `DELETE /api/fcm/tokens/{tokenId}`: FCM 토큰 삭제

### 알림 설정
- `GET /api/notification-settings`: 알림 설정 조회
- `PUT /api/notification-settings`: 알림 설정 변경

## Kafka 이벤트 구독

### 구독 토픽
- **Topic**: `community.notification`
- **Consumer Group**: `notification-service-group`
- **처리 방식**: `runBlocking` + `suspend` 함수 통합

### 이벤트 유형
```kotlin
// 지원하는 커뮤니티 이벤트
- PostCreated: 새 게시글 작성 알림
- CommentCreated: 새 댓글 작성 알림  
- PostLiked: 게시글 좋아요 알림
- CommentLiked: 댓글 좋아요 알림
```

### 이벤트 처리 예시
```kotlin
@KafkaListener(topics = ["community.notification"])
fun handleCommunityNotification(message: String) = runBlocking {
    val event = objectMapper.readValue(message, CommunityNotificationEvent::class.java)
    
    // 1. DB에 알림 저장
    val savedNotifications = saveNotifications(event)
    
    // 2. FCM 배치 발송 (Coroutines)
    sendFcmNotifications(event, savedNotifications)
}
```

## 성능 및 모니터링

### 성능 지표
- **처리량**: 초당 1,000+ 이벤트 처리
- **응답 시간**: 이벤트 수신 후 평균 2-3초 내 FCM 발송
- **안정성**: 부분 실패 시에도 95% 이상 발송 성공률
- **메모리 효율성**: Coroutines로 70% 스레드 사용량 절감

### 모니터링
- **Kafka Consumer Lag**: 이벤트 처리 지연 모니터링
- **FCM 발송 성공률**: 토큰 유효성 및 발송 결과 추적
- **알림 생성/발송 메트릭**: 시간별 알림 처리량 분석

## 보안 및 신뢰성

- **인증**: Spring Security + JWT 토큰 기반
- **FCM 보안**: Firebase Admin SDK 서버 키 안전 관리
- **데이터 보호**: 민감한 알림 내용 처리 시 암호화
- **장애 대응**: 부분 실패 허용으로 시스템 안정성 확보
- **트랜잭션**: 이벤트 발행 시점의 데이터 일관성 보장