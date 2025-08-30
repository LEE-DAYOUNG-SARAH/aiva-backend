# AIVA - 생성형 AI 육아 상담 서비스 👶🤖

AIVA는 부모를 위한 **AI 육아 비서**입니다.  
아이 프로필을 기반으로 맞춤형 상담(Q&A)을 제공하고, 최신 육아 정책·혜택 정보를 안내하며,  
부모 간의 커뮤니티와 실시간 알림 기능까지 지원합니다.

## 🏆 Achievement
- 2025 서울 우먼해커톤 **결선 진출**
- "여성·가족 친화 서비스" 주제로 **AI 육아 비서 서버 및 백엔드 개발** 담당

---

## 🏗️ 시스템 아키텍처

### 서비스 구성
- **User Service**: 사용자 관리, 인증, 아이 정보 관리
- **Chat Service**: AI 채팅, FAQ 관리
- **Community Service**: 커뮤니티 게시글, 댓글, 좋아요, 신고
- **Notification Service**: 알림 설정, 알림 발송 관리
- **Subscription Service**: 구독 관리, 결제 처리
- **API Gateway**: 라우팅, 인증, 로드밸런싱

### 인프라 구성
- **Database**: MySQL (각 서비스별 독립 DB)
- **Cache**: Redis (캐싱 및 세션 관리)
- **Message Queue**: AWS SNS + SQS (알림 처리)
- **Container**: Docker & Kubernetes
- **API Gateway**: Spring Cloud Gateway

## 📁 프로젝트 구조

```
aiva-msa/
├── services/
│   ├── user-service/          # 사용자 관리 서비스
│   ├── chat-service/          # 채팅 서비스
│   ├── community-service/     # 커뮤니티 서비스
│   ├── notification-service/  # 알림 서비스
│   └── subscription-service/  # 구독 서비스
├── infrastructure/
│   ├── docker/               # Docker 설정
│   ├── kubernetes/           # K8s 설정
│   ├── terraform/            # AWS 인프라 설정
│   └── gateway/              # API Gateway 설정
├── shared/
│   ├── libs/                 # 공통 라이브러리
│   └── schemas/              # 공통 스키마
└── scripts/                  # 배포, 설정 스크립트
```

## 🛠️ 기술 스택

- **Backend**: Kotlin + Spring Boot 3
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA
- **Cache**: Redis
- **Message Queue**: AWS SNS/SQS
- **Container**: Docker + Kubernetes
- **API Gateway**: Spring Cloud Gateway
- **Service Discovery**: Eureka Server
- **Configuration**: Spring Cloud Config
- **Circuit Breaker**: Resilience4j
- **Monitoring**: Micrometer + Spring Boot Actuator
- **Documentation**: SpringDoc OpenAPI 3

## 📊 서비스별 포트 및 책임

| 서비스 | 포트 | 데이터베이스 | 주요 기능 |
|--------|------|-------------|-----------|
| API Gateway | 8080 | - | 라우팅, 인증, 로드밸런싱 |
| User Service | 8081 | aiva_user | 사용자/아이 정보, OAuth 인증 |
| Chat Service | 8082 | aiva_chat | AI 채팅, FAQ |
| Community Service | 8083 | aiva_community | 게시글, 댓글, 좋아요 |
| Notification Service | 8084 | aiva_notification | 알림 설정, 푸시 발송 |
| Subscription Service | 8085 | aiva_subscription | 구독, 결제 |

## 🚀 빠른 시작

### 1. 개발 환경 요구사항
- JDK 17+
- Docker & Docker Compose
- MySQL 8.0 (Docker로 제공)
- Redis (Docker로 제공)

### 2. 개발 환경 설정
```bash
# 의존성 설치 및 인프라 설정
./scripts/setup-dev.sh

# 전체 서비스 빌드
./scripts/build-all.sh

# 전체 서비스 시작
./scripts/start-all-services.sh
```

### 3. 개별 서비스 실행
```bash
# 인프라 서비스 먼저 시작
docker-compose up -d

# 각 서비스 개별 실행
cd services/user-service && ./gradlew bootRun
cd services/chat-service && ./gradlew bootRun
cd services/community-service && ./gradlew bootRun
cd services/notification-service && ./gradlew bootRun
cd services/subscription-service && ./gradlew bootRun
cd infrastructure/gateway && ./gradlew bootRun
```

## 📋 API 엔드포인트

### API Gateway (http://localhost:8080)
- 모든 API 요청은 Gateway를 통해 라우팅됩니다
- Rate Limiting 적용
- JWT 인증 처리

### 서비스별 API
| 서비스 | 엔드포인트 | 설명 |
|--------|-----------|------|
| User Service | `/api/users/**` | 사용자 관리, 인증 |
| User Service | `/api/children/**` | 아이 정보 관리 |
| User Service | `/api/devices/**` | 디바이스 관리 |
| Chat Service | `/api/chats/**` | 채팅 관리 |
| Chat Service | `/api/messages/**` | 메시지 관리 |
| Chat Service | `/api/faqs/**` | FAQ 관리 |
| Community Service | `/api/posts/**` | 게시글 관리 |
| Community Service | `/api/comments/**` | 댓글 관리 |
| Community Service | `/api/likes/**` | 좋아요 관리 |
| Community Service | `/api/reports/**` | 신고 관리 |
| Notification Service | `/api/notifications/**` | 알림 관리 |
| Notification Service | `/api/notification-settings/**` | 알림 설정 |
| Subscription Service | `/api/subscriptions/**` | 구독 관리 |
| Subscription Service | `/api/plans/**` | 구독 플랜 |
| Subscription Service | `/api/payments/**` | 결제 관리 |

## 🔧 설정 관리

### 환경별 설정
각 서비스는 다음 환경 설정을 지원합니다:
- `application.yml` (기본)
- `application-local.yml` (로컬 개발)
- `application-dev.yml` (개발 서버)
- `application-staging.yml` (스테이징)
- `application-prod.yml` (운영)

### 환경 변수
주요 환경 변수들:
```bash
# 데이터베이스
MYSQL_ROOT_PASSWORD=root123
MYSQL_USER=aiva
MYSQL_PASSWORD=aiva123

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_SECRET=aiva-jwt-secret-key

# AWS (알림 시스템)
AWS_SNS_TOPIC_ARN=arn:aws:sns:...
AWS_SQS_QUEUE_URL=https://sqs...
AWS_ACCESS_KEY_ID=...
AWS_SECRET_ACCESS_KEY=...

# 외부 API
OPENAI_API_KEY=your-openai-api-key
FCM_SERVICE_ACCOUNT_KEY=path-to-firebase-json

# 결제
TOSS_CLIENT_KEY=test_ck_...
TOSS_SECRET_KEY=test_sk_...
```

## 🐳 Docker 배포

### 로컬 개발 환경
```bash
# 인프라 서비스만 Docker로 실행
docker-compose up -d

# 애플리케이션은 로컬에서 실행
./scripts/start-all-services.sh
```

### 전체 Docker 배포 (향후 Dockerfile 추가 예정)
```bash
# 전체 스택 Docker 실행
docker-compose up -d
```

## ☸️ Kubernetes 배포

### 네임스페이스 생성
```bash
kubectl apply -f infrastructure/kubernetes/namespace.yaml
```

### 데이터베이스 배포
```bash
kubectl apply -f infrastructure/kubernetes/mysql-deployment.yaml
```

### 애플리케이션 배포 (향후 추가 예정)
```bash
kubectl apply -f infrastructure/kubernetes/services/
```

## 🏗️ AWS 인프라 배포

### Terraform으로 AWS 리소스 생성
```bash
cd infrastructure/terraform
terraform init
terraform plan
terraform apply
```

생성되는 AWS 리소스:
- SNS Topic (알림 발송)
- SQS Queue (알림 처리)
- Lambda Function (FCM 푸시 처리)
- IAM Roles & Policies

## 📊 모니터링

### 헬스체크
- 각 서비스: `http://localhost:808X/actuator/health`
- Gateway: `http://localhost:8080/actuator/health`

### 메트릭
- 각 서비스: `http://localhost:808X/actuator/metrics`
- 서비스 정보: `http://localhost:808X/actuator/info`

## 🛠️ 개발 도구

### 새 서비스 생성
```bash
./scripts/create-service.sh new-service-name
```

### 데이터베이스 마이그레이션
```bash
# 각 서비스에서 Flyway 마이그레이션 실행
cd services/user-service
./gradlew flywayMigrate
```

### 테스트 실행
```bash
# 전체 테스트
./gradlew test

# 특정 서비스 테스트
cd services/user-service
./gradlew test
```

## 🔐 보안

### JWT 인증
- Bearer Token 기반 인증
- Gateway에서 토큰 검증
- 서비스 간 통신 시 토큰 전달

### API Rate Limiting
- Redis 기반 Rate Limiting
- 서비스별 다른 제한값 적용

### CORS 설정
- Gateway에서 CORS 처리
- Origin 패턴 기반 허용

## 📈 성능 최적화

### 캐싱 전략
- Redis를 통한 세션 관리
- 자주 조회되는 데이터 캐싱
- JPA 2차 캐시 활용

### 데이터베이스 최적화
- 서비스별 독립 데이터베이스
- 인덱스 최적화
- 연결 풀 설정

## 🐛 트러블슈팅

### 일반적인 문제들

1. **포트 충돌**
   ```bash
   # 사용 중인 포트 확인
   lsof -i :8080
   ```

2. **데이터베이스 연결 실패**
   ```bash
   # 데이터베이스 상태 확인
   docker-compose ps
   docker-compose logs mysql-user
   ```

3. **Redis 연결 실패**
   ```bash
   # Redis 상태 확인
   docker-compose logs redis
   redis-cli ping
   ```

### 로그 확인
```bash
# 서비스별 로그 확인
tail -f services/user-service/logs/application.log

# Docker 컨테이너 로그
docker-compose logs -f mysql-user
```
