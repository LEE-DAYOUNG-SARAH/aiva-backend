# Notification Service

AIVA 백엔드 시스템의 알림 서비스입니다. FCM 푸시 알림을 통한 실시간 알림 기능을 제공합니다.

## 주요 기능

### 알림 발송
- **FCM 푸시 알림**: Firebase Cloud Messaging을 통한 모바일 푸시 알림
- **일괄 알림**: 대량 알림의 효율적인 처리
- **실시간 알림**: 즉시 발송 및 스케줄된 알림 처리

### 디바이스 관리
- **FCM 토큰 관리**: 사용자 디바이스의 FCM 토큰 등록/갱신
- **디바이스 등록**: 다중 디바이스 지원
- **토큰 유효성 검증**: 만료된 토큰 자동 정리

### 알림 설정
- **개인화된 알림 설정**: 사용자별 알림 선호도 관리
- **알림 타입별 설정**: 카테고리별 알림 on/off
- **시간대 설정**: 방해 금지 시간 설정

### 알림 히스토리
- **발송 기록**: 모든 알림 발송 기록 보관
- **읽음 상태 추적**: 알림 읽음/읽지 않음 상태 관리
- **통계**: 알림 발송 및 열람 통계

## 기술 스택

- **Framework**: Spring Boot, Spring Data JPA
- **Push Notification**: Firebase Cloud Messaging (FCM)
- **Database**: MySQL
- **Cache**: Redis
- **Message Streaming**: Apache Kafka
- **Async Processing**: Spring Async

## 아키텍처

### 주요 컴포넌트
- **Notification Domain**: 알림 핵심 비즈니스 로직
- **Device Domain**: 디바이스 관리
- **FCM Domain**: FCM 토큰 관리
- **Setting Domain**: 알림 설정 관리
- **Event Listeners**: Kafka 이벤트 구독

### 알림 플로우
1. 이벤트 수신 (Kafka)
2. 알림 설정 확인
3. 디바이스 정보 조회
4. 알림 생성 및 발송
5. 발송 결과 기록

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

## 이벤트 구독

### 구독하는 이벤트
- `PostCreated`: 새 게시글 알림
- `CommentCreated`: 새 댓글 알림
- `PostLiked`: 게시글 좋아요 알림
- `CommentLiked`: 댓글 좋아요 알림
- `SubscriptionExpiring`: 구독 만료 예정 알림

## 보안

- Spring Security 기반 인증
- FCM 서버 키 보안 관리
- 개인정보 보호를 위한 알림 내용 암호화
- JWT 토큰 기반 사용자 인증