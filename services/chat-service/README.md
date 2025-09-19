# Chat Service

AIVA 백엔드 시스템의 채팅 서비스입니다. AI 기반 대화 기능을 제공하며, OpenAI API를 활용한 지능형 채팅 시스템을 구현합니다.

## 주요 기능

### AI 채팅
- **OpenAI API 연동**: GPT 모델을 활용한 지능형 대화
- **대화 컨텍스트 관리**: 연속적인 대화 흐름 유지
- **실시간 채팅**: WebFlux 기반의 리액티브 채팅 처리

### 대화 관리
- **채팅 세션 관리**: 사용자별 채팅 세션 추적
- **대화 기록 저장**: 채팅 히스토리 영구 보관
- **컨텍스트 캐싱**: Redis를 통한 빠른 대화 컨텍스트 접근

## 기술 스택

- **Framework**: Spring Boot, Spring WebFlux
- **AI Integration**: OpenAI GPT API
- **Database**: MySQL (대화 기록), Redis (캐시)
- **Security**: Spring Security
- **Migration**: Flyway

## 아키텍처

### 주요 컴포넌트
- **Chat Controller**: 채팅 API 엔드포인트
- **AI Service**: OpenAI API 연동 및 대화 처리
- **Chat Repository**: 대화 데이터 영속화
- **Context Manager**: 대화 컨텍스트 관리

### 데이터 플로우
1. 사용자 메시지 수신
2. 대화 컨텍스트 로드
3. OpenAI API 호출
4. 응답 생성 및 저장
5. 클라이언트에 응답 전달

## 실행 방법

```bash
# 애플리케이션 실행
./gradlew :services:chat-service:bootRun

# 테스트 실행
./gradlew :services:chat-service:test
```

## 환경 설정

필요한 환경 변수:
- `OPENAI_API_KEY`: OpenAI API 키
- `MYSQL_URL`: MySQL 데이터베이스 URL
- `REDIS_URL`: Redis 서버 URL

## API 엔드포인트

### 채팅 관련
- `POST /api/chat/send`: 메시지 전송
- `GET /api/chat/history/{sessionId}`: 채팅 기록 조회
- `DELETE /api/chat/session/{sessionId}`: 채팅 세션 삭제

## 보안

- Spring Security 기반 인증
- JWT 토큰을 통한 사용자 인증
- API 키 보안 관리