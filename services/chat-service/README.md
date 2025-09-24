# Chat Service

AIVA 백엔드 시스템의 채팅 서비스입니다. AI 기반 실시간 대화 기능을 제공하며, Spring WebFlux와 Redis를 활용한 분산 리액티브 채팅 시스템을 구현합니다.

## 주요 기능

### 실시간 스트리밍 채팅
- **Server-Sent Events(SSE)**: 실시간 AI 응답 스트리밍
- **논블로킹 I/O**: Spring WebFlux 기반 비동기 처리
- **스트림 취소**: 실시간 채팅 중단 및 취소 기능
- **다중 기기 지원**: 동일 사용자의 여러 기기별 독립적 세션 관리

### 분산 세션 관리
- **Redis 기반 세션 저장소**: 분산 환경에서 세션 공유 및 관리
- **Pub/Sub 기반 취소**: Redis 채널을 통한 실시간 스트림 취소 신호
- **TTL 자동 정리**: 2시간 자동 만료로 메모리 최적화
- **다중 서버 인스턴스 지원**: 로드밸런서 환경에서 세션 일관성 유지

### AI 채팅 통합
- **외부 AI API 연동**: WebClient를 통한 논블로킹 API 호출
- **대화 컨텍스트 관리**: 연속적인 대화 흐름 유지
- **메타데이터 처리**: 출처 정보 및 응답 상태 관리

## 기술 스택

- **Framework**: Spring Boot, Spring WebFlux
- **Session Store**: Redis (분산 세션 관리, Pub/Sub)
- **Database**: MySQL
- **AI Integration**: 외부 AI API
- **Security**: Spring Security
- **Migration**: Flyway
- **Logging**: KotlinLogging
- **Testing**: JUnit 5, Mockito, Reactor Test

## 아키텍처

### 주요 컴포넌트
- **ChatController**: SSE 스트리밍 및 취소 API 엔드포인트
- **ChatStreamService**: 실시간 스트림 관리 및 Redis 세션 제어
- **ActiveChatStreamService**: 분산 세션 저장소 및 Pub/Sub 관리
- **AiService**: 외부 AI API 논블로킹 연동
- **ChatManagementService**: 채팅 데이터 영속화 및 관리
- **AiResponseParser**: AI 응답 스트림 파싱 및 이벤트 처리

### 리액티브 스트리밍 플로우
1. **클라이언트 연결**: SSE 연결 생성 및 Redis 세션 등록
2. **스트림 시작**: AI API로부터 Flux<String> 스트림 수신
3. **실시간 전송**: 델타 응답을 클라이언트에 실시간 전송
4. **취소 처리**: Redis Pub/Sub을 통한 분산 취소 신호 처리
5. **세션 정리**: 스트림 완료 시 Redis 세션 자동 정리

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

### 채팅 관리
- `POST /api/chat/create`: 새 채팅방 생성
  - Headers: `X-User-Id: {userId}`
  - Response: 생성된 채팅방 정보

### 실시간 스트리밍
- `POST /api/chat/{chatId}/stream`: 실시간 AI 채팅 스트림
  - Headers: `X-User-Id: {userId}`, `X-Session-Id: {sessionId}`
  - Content-Type: `text/event-stream`
  - Body: `{"content": "사용자 메시지"}`
  - Response: Server-Sent Events 스트림

### 스트림 제어
- `POST /api/chat/{chatId}/cancel`: 진행 중인 스트림 취소
  - Headers: `X-Session-Id: {sessionId}`
  - Response: 취소 성공/실패 정보

## 사용 예시

### 채팅 스트림 연결
```javascript
// 채팅방 생성
const chatResponse = await fetch('/api/chat/create', {
  method: 'POST',
  headers: { 'X-User-Id': 'user-123' }
});
const { chatId } = await chatResponse.json();

// SSE 스트림 연결
const eventSource = new EventSource('/api/chat/' + chatId + '/stream', {
  method: 'POST',
  headers: {
    'X-User-Id': 'user-123',
    'X-Session-Id': 'session-456'
  },
  body: JSON.stringify({ content: '안녕하세요!' })
});

eventSource.onmessage = (event) => {
  console.log('AI Response:', event.data);
};

// 스트림 취소
await fetch('/api/chat/' + chatId + '/cancel', {
  method: 'POST',
  headers: { 'X-Session-Id': 'session-456' }
});
```

## 성능 특징

### 리액티브 프로그래밍 장점
- **논블로킹 I/O**: 높은 동시성과 적은 스레드 사용량
- **백프레셔 지원**: 클라이언트와 서버 간 적절한 속도 조절
- **메모리 효율성**: 스트림 기반 처리로 메모리 사용량 최적화

### 분산 환경 최적화
- **세션 공유**: Redis를 통한 서버 간 세션 일관성
- **자동 정리**: TTL 기반 메모리 누수 방지
- **실시간 취소**: Pub/Sub 패턴으로 즉시 응답 가능한 취소 처리

## 보안

- **인증 헤더**: `X-User-Id`를 통한 사용자 식별
- **세션 격리**: `X-Session-Id`를 통한 기기별 독립적 세션
- **리소스 보호**: TTL을 통한 자동 세션 만료
- **API 보안**: Spring Security 통합 인증

## 모니터링 및 로깅

- **KotlinLogging**: 구조화된 로그 출력
- **스트림 상태 추적**: 세션 생성/종료/취소 이벤트 로깅
- **성능 메트릭**: Redis 연결 상태 및 활성 세션 수 모니터링