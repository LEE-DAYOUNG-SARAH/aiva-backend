# Community Service

AIVA 백엔드 시스템의 커뮤니티 서비스입니다. 사용자 간 소통을 위한 게시글, 댓글, 좋아요 등의 커뮤니티 기능을 제공하며, Redis 기반 고성능 캐싱과 gRPC를 통한 서비스 간 통신을 지원합니다.

## 주요 기능

### 게시글 관리
- **게시글 CRUD**: 생성, 조회, 수정, 삭제
- **게시글 좋아요**: 사용자의 게시글 좋아요/취소
- **게시글 목록**: 커서 기반 페이지네이션으로 최적화된 목록 조회
- **인기 게시물**: 좋아요/댓글 수 기반 인기 게시물 정렬
- **Redis 캐싱**: Hash + SortedSet 구조로 고성능 캐싱

### 댓글 시스템
- **댓글 CRUD**: 댓글 생성, 조회, 수정, 삭제
- **댓글 좋아요**: 댓글에 대한 좋아요 기능
- **대댓글 지원**: 계층형 댓글 구조

### 사용자 정보 관리
- **gRPC 통신**: User Service와 gRPC로 사용자 정보 조회
- **사용자 프로필 캐싱**: Redis를 통한 사용자 정보 캐싱
- **JWT 토큰 기반**: 사용자 인증 정보 (nickname, profileUrl 포함)

### 이벤트 스트리밍 & 알림
- **Kafka 연동**: 커뮤니티 활동에 대한 이벤트 발행
- **멱등성 보장**: 자연키 기반 이벤트 ID로 중복 처리 방지
- **실시간 알림**: 좋아요, 댓글 등의 활동 알림

## 기술 스택

- **Framework**: Spring Boot 3, Spring Data JPA
- **Database**: MySQL 8.0
- **Cache**: Redis (Spring Data Redis)
- **Message Streaming**: Apache Kafka
- **Service Communication**: gRPC
- **Security**: Spring Security, JWT
- **Migration**: Flyway

## 아키텍처

### 주요 컴포넌트
- **Post Domain**: 게시글 관련 비즈니스 로직
- **Comment Domain**: 댓글 관련 비즈니스 로직
- **Like System**: 좋아요 기능
- **User Integration**: gRPC 기반 사용자 서비스 연동
- **Event Publisher**: Kafka 이벤트 발행
- **Redis Cache Layer**: 고성능 캐싱 레이어

### 도메인 구조
```
community/
├── domain/
│   ├── post/                # 게시글 도메인
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   ├── comment/             # 댓글 도메인
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   └── user/                # 사용자 연동
│       └── UserGrpcClient   # gRPC 클라이언트
└── global/
    ├── event/               # 이벤트 처리
    │   └── notification/    # 알림 이벤트
    └── cache/               # 캐시 확장 함수
```

### Redis 캐싱 아키텍처

#### SortedSet + Hash 구조
```
Redis Keys:
├── community:posts:latest          # SortedSet (타임스탬프 기반 정렬)
├── community:post:{postId}         # Hash (게시물 상세 정보)
└── user:{userId}:profile           # Hash (사용자 프로필 캐시)
```

#### 캐싱 전략
- **게시물 저장**: Repository로 Hash 저장 + SortedSet에 ID 추가
- **목록 조회**: SortedSet에서 ID 목록 → Hash에서 상세 정보 배치 조회
- **TTL 관리**: 24시간 자동 만료
- **커서 페이지네이션**: 타임스탬프 기반 효율적 페이징

## 실행 방법

```bash
# 애플리케이션 실행
./gradlew :services:community-service:bootRun

# 테스트 실행
./gradlew :services:community-service:test

# 빌드
./gradlew :services:community-service:build
```

## 환경 설정

필요한 환경 변수:
```properties
# 데이터베이스
MYSQL_URL=jdbc:mysql://localhost:3306/aiva_community
MYSQL_USERNAME=aiva
MYSQL_PASSWORD=aiva123

# Redis
REDIS_URL=redis://localhost:6379

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# gRPC
GRPC_USER_SERVICE_HOST=localhost
GRPC_USER_SERVICE_PORT=9090

# JWT
JWT_SECRET=aiva-jwt-secret-key
```

## API 엔드포인트

### 게시글 관리
- `POST /api/v1/posts`: 게시글 생성
- `GET /api/v1/posts`: 게시글 목록 조회 (오프셋 기반)
- `GET /api/v1/posts/cursor`: 게시글 목록 조회 (커서 기반)
- `GET /api/v1/posts/popular`: 인기 게시글 조회
- `GET /api/v1/posts/me`: 내 게시글 조회
- `GET /api/v1/posts/{postId}`: 게시글 상세 조회
- `PUT /api/v1/posts/{postId}`: 게시글 수정
- `DELETE /api/v1/posts/{postId}`: 게시글 삭제

### 좋아요
- `POST /api/v1/posts/{postId}/like`: 게시글 좋아요
- `DELETE /api/v1/posts/{postId}/like`: 게시글 좋아요 취소
- `POST /api/v1/comments/{commentId}/like`: 댓글 좋아요
- `DELETE /api/v1/comments/{commentId}/like`: 댓글 좋아요 취소

### 댓글 관리
- `POST /api/v1/posts/{postId}/comments`: 댓글 생성
- `POST /api/v1/comments/{commentId}/reply`: 대댓글 생성
- `GET /api/v1/posts/{postId}/comments`: 댓글 목록 조회
- `PUT /api/v1/comments/{commentId}`: 댓글 수정
- `DELETE /api/v1/comments/{commentId}`: 댓글 삭제

## 이벤트 & 알림

### 발행하는 이벤트 (Kafka Topics)
- `community.notification`: 알림 이벤트
  - **PostLiked**: 게시글 좋아요 (`POST_LIKED`)
  - **CommentCreated**: 댓글 생성 (`COMMENT_CREATED`) 
  - **ReplyCreated**: 대댓글 생성 (`REPLY_CREATED`)
  - **CommentLiked**: 댓글 좋아요 (`COMMENT_LIKED`)

### 이벤트 멱등성
```kotlin
// 자연키 형태 이벤트 ID
eventId = "${eventType}:${targetUserId}:${actorUserId}:${resourceId}[:${parentResourceId}]"

// 예시
"POST_LIKED:user123:user456:post789"
"COMMENT_CREATED:user123:user456:comment101:post789"
```

## gRPC 서비스 연동

### User Service 통신
```protobuf
service UserService {
  rpc GetUserProfile(GetUserProfileRequest) returns (UserProfile);
  rpc GetUserProfiles(GetUserProfilesRequest) returns (GetUserProfilesResponse);
}

message UserProfile {
  string userId = 1;
  string nickname = 2;
  string profileImageUrl = 3;
}
```

### 캐싱 전략
1. **Cache-First**: Redis에서 우선 조회
2. **gRPC Fallback**: 캐시 미스 시 gRPC 호출
3. **Batch Optimization**: 다중 사용자 정보 일괄 조회
4. **Auto Caching**: gRPC 응답 자동 캐싱

## 성능 최적화

### 페이지네이션
- **오프셋 기반**: 호환성을 위한 기본 페이지네이션
- **커서 기반**: 대용량 데이터 효율적 처리
- **24시간 컷오프**: 최신 데이터 우선 캐시 활용

### 캐시 최적화
- **Redis Repository**: @RedisHash 엔티티 활용
- **하이브리드 접근**: Repository + RedisTemplate 조합
- **TTL 관리**: 자동 만료 및 수동 갱신
- **메모리 효율성**: SortedSet으로 목록 관리

## 보안

### 인증 & 인가
- **JWT 토큰**: Bearer Token 기반 인증
- **사용자 정보**: 토큰에서 nickname, profileUrl 추출
- **권한 검증**: 작성자 본인만 수정/삭제 가능
- **헤더 주입**: Gateway에서 사용자 정보 헤더 자동 주입

### 데이터 보안
- **자연키 이벤트**: 예측 가능한 이벤트 ID로 보안 강화
- **입력 검증**: DTO 기반 요청 데이터 검증
- **SQL 인젝션 방지**: JPA/MyBatis 파라미터 바인딩

## 트러블슈팅

### 일반적인 문제들

1. **Redis 연결 실패**
   ```bash
   # Redis 상태 확인
   redis-cli ping
   docker-compose logs redis
   ```

2. **gRPC 연결 실패**
   ```bash
   # User Service 상태 확인
   grpcurl -plaintext localhost:9090 list
   ```

3. **캐시 불일치**
   ```bash
   # Redis 캐시 확인
   redis-cli keys "community:*"
   redis-cli hgetall "community:post:uuid"
   ```

4. **Kafka 이벤트 미발송**
   ```bash
   # Kafka 토픽 확인
   kafka-topics.sh --list --bootstrap-server localhost:9092
   kafka-console-consumer.sh --topic community.notification --bootstrap-server localhost:9092
   ```

### 로그 확인
```bash
# 애플리케이션 로그
tail -f services/community-service/logs/application.log

# Redis 캐시 디버그
# application.yml에서 logging.level.com.aiva.common.redis=DEBUG

# gRPC 통신 디버그  
# application.yml에서 logging.level.com.aiva.community.domain.user=DEBUG
```

## 개발 가이드

### 새 기능 추가
1. **도메인 모델**: Entity, DTO 정의
2. **Repository**: JPA Repository 구현
3. **Service**: 비즈니스 로직 구현
4. **Controller**: API 엔드포인트 정의
5. **캐시 연동**: Redis 캐시 전략 적용
6. **이벤트 발행**: 필요시 Kafka 이벤트 추가

### 테스트 작성
- **단위 테스트**: Service, Repository 레이어
- **통합 테스트**: Controller, Cache, gRPC 연동
- **성능 테스트**: 대용량 데이터 페이지네이션

### 캐시 전략 설계
- **데이터 특성**: 읽기/쓰기 패턴 분석
- **TTL 설정**: 데이터 신선도 요구사항
- **무효화 전략**: 업데이트 시 캐시 갱신 방법
- **메모리 사용량**: Redis 메모리 효율성 고려