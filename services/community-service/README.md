# Community Service

AIVA 백엔드 시스템의 커뮤니티 서비스입니다. 사용자 간 소통을 위한 게시글, 댓글, 좋아요 등의 커뮤니티 기능을 제공합니다.

## 주요 기능

### 게시글 관리
- **게시글 CRUD**: 생성, 조회, 수정, 삭제
- **게시글 좋아요**: 사용자의 게시글 좋아요/취소
- **게시글 목록**: 페이징된 게시글 목록 조회

### 댓글 시스템
- **댓글 CRUD**: 댓글 생성, 조회, 수정, 삭제
- **댓글 좋아요**: 댓글에 대한 좋아요 기능
- **대댓글 지원**: 계층형 댓글 구조

### 이벤트 스트리밍
- **Kafka 연동**: 커뮤니티 활동에 대한 이벤트 발행
- **실시간 알림**: 좋아요, 댓글 등의 활동 알림

## 기술 스택

- **Framework**: Spring Boot, Spring Data JPA
- **Database**: MySQL
- **Cache**: Redis
- **Message Streaming**: Apache Kafka
- **Security**: Spring Security
- **Migration**: Flyway

## 아키텍처

### 주요 컴포넌트
- **Post Domain**: 게시글 관련 비즈니스 로직
- **Comment Domain**: 댓글 관련 비즈니스 로직
- **Like System**: 좋아요 기능
- **Event Publisher**: Kafka 이벤트 발행

### 도메인 구조
```
community/
├── domain/
│   ├── post/          # 게시글 도메인
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   └── entity/
│   └── comment/       # 댓글 도메인
│       ├── controller/
│       ├── service/
│       ├── repository/
│       └── entity/
```

## 실행 방법

```bash
# 애플리케이션 실행
./gradlew :services:community-service:bootRun

# 테스트 실행
./gradlew :services:community-service:test
```

## 환경 설정

필요한 환경 변수:
- `MYSQL_URL`: MySQL 데이터베이스 URL
- `REDIS_URL`: Redis 서버 URL
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka 서버 주소

## API 엔드포인트

### 게시글 관리
- `POST /api/community/posts`: 게시글 생성
- `GET /api/community/posts`: 게시글 목록 조회
- `GET /api/community/posts/{postId}`: 게시글 상세 조회
- `PUT /api/community/posts/{postId}`: 게시글 수정
- `DELETE /api/community/posts/{postId}`: 게시글 삭제

### 좋아요
- `POST /api/community/posts/{postId}/like`: 게시글 좋아요
- `DELETE /api/community/posts/{postId}/like`: 게시글 좋아요 취소

### 댓글 관리
- `POST /api/community/posts/{postId}/comments`: 댓글 생성
- `GET /api/community/posts/{postId}/comments`: 댓글 목록 조회
- `PUT /api/community/comments/{commentId}`: 댓글 수정
- `DELETE /api/community/comments/{commentId}`: 댓글 삭제
- `POST /api/community/comments/{commentId}/like`: 댓글 좋아요

## 이벤트

### 발행하는 이벤트
- `PostCreated`: 게시글 생성
- `PostLiked`: 게시글 좋아요
- `CommentCreated`: 댓글 생성
- `CommentLiked`: 댓글 좋아요

## 보안

- Spring Security 기반 인증/인가
- 작성자 본인만 수정/삭제 가능
- JWT 토큰 기반 사용자 인증