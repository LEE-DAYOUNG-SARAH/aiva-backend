# Subscription Service

AIVA 백엔드 시스템의 구독 서비스입니다. 사용자의 구독 관리, 결제 처리, 구독 상태 추적 등의 기능을 제공합니다.

## 주요 기능

### 구독 관리
- **구독 플랜 관리**: 다양한 구독 요금제 제공
- **구독 생성/취소**: 사용자 구독 시작 및 종료
- **구독 상태 추적**: 활성, 만료, 일시정지 상태 관리
- **자동 갱신**: 구독 자동 갱신 설정 및 처리

### 결제 처리
- **결제 연동**: 외부 결제 시스템과 연동
- **결제 기록**: 모든 결제 내역 추적
- **환불 처리**: 구독 취소 시 환불 로직
- **결제 실패 처리**: 결제 실패 시 재시도 및 알림

### 구독 갱신
- **스케줄된 갱신**: Quartz를 이용한 정기적 갱신 처리
- **갱신 알림**: 갱신 전 사용자 알림
- **만료 처리**: 갱신 실패 시 구독 만료 처리

## 기술 스택

- **Framework**: Spring Boot, Spring Data JPA
- **Database**: MySQL
- **Cache**: Redis
- **Scheduler**: Quartz
- **HTTP Client**: Spring WebFlux
- **Async Processing**: Spring Async
- **Migration**: Flyway

## 아키텍처

### 주요 컴포넌트
- **Subscription Domain**: 구독 핵심 비즈니스 로직
- **Payment Domain**: 결제 처리 로직
- **Plan Domain**: 구독 플랜 관리
- **Renewal Scheduler**: 자동 갱신 스케줄러

### 구독 생명주기
1. 구독 요청
2. 결제 처리
3. 구독 활성화
4. 정기 갱신
5. 만료/취소 처리

## 실행 방법

```bash
# 애플리케이션 실행
./gradlew :services:subscription-service:bootRun

# 테스트 실행
./gradlew :services:subscription-service:test
```

## 환경 설정

필요한 환경 변수:
- `MYSQL_URL`: MySQL 데이터베이스 URL
- `REDIS_URL`: Redis 서버 URL
- `PAYMENT_GATEWAY_URL`: 결제 게이트웨이 URL
- `PAYMENT_API_KEY`: 결제 시스템 API 키

## API 엔드포인트

### 구독 관리
- `POST /api/subscriptions`: 구독 생성
- `GET /api/subscriptions`: 사용자 구독 목록 조회
- `GET /api/subscriptions/{subscriptionId}`: 구독 상세 조회
- `PUT /api/subscriptions/{subscriptionId}/cancel`: 구독 취소
- `PUT /api/subscriptions/{subscriptionId}/pause`: 구독 일시정지
- `PUT /api/subscriptions/{subscriptionId}/resume`: 구독 재개

### 구독 플랜
- `GET /api/subscription-plans`: 구독 플랜 목록 조회
- `GET /api/subscription-plans/{planId}`: 플랜 상세 정보

### 결제 관리
- `POST /api/payments`: 결제 처리
- `GET /api/payments`: 결제 내역 조회
- `POST /api/payments/{paymentId}/refund`: 환불 처리

### 갱신 관리
- `PUT /api/subscriptions/{subscriptionId}/auto-renewal`: 자동 갱신 설정
- `POST /api/subscriptions/{subscriptionId}/renew`: 수동 갱신

## 스케줄된 작업

### 일일 작업
- **만료 예정 구독 확인**: 7일, 3일, 1일 전 알림
- **갱신 처리**: 만료일 도래 시 자동 갱신
- **만료 처리**: 갱신 실패 시 구독 만료

### 주간 작업
- **구독 통계 생성**: 주간 구독 현황 리포트
- **결제 실패 재시도**: 실패한 결제 재처리

## 보안

- Spring Security 기반 인증
- 결제 정보 암호화
- API 키 및 민감 정보 보안 관리

## 모니터링

- 구독 상태 메트릭스
- 결제 성공/실패 률
- 갱신 성공/실패 률
- 고객 이탈률 추적