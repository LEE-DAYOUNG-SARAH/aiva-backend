# User Service

AIVA 백엔드 시스템의 사용자 서비스입니다. 사용자 인증, 회원 관리, 프로필 관리 등 사용자와 관련된 모든 기능을 담당합니다.

## 주요 기능

### 인증 시스템
- **JWT 기반 인증**: 토큰 기반 사용자 인증
- **로그인/로그아웃**: 사용자 세션 관리
- **토큰 갱신**: 액세스 토큰 및 리프레시 토큰 관리
- **보안**: Spring Security 기반 보안 체계

### 사용자 관리
- **회원가입**: 새 사용자 등록
- **프로필 관리**: 사용자 정보 조회 및 수정
- **계정 설정**: 개인 설정 관리
- **회원 탈퇴**: 계정 삭제 및 데이터 처리

### 자녀 관리
- **자녀 프로필**: 자녀 정보 등록 및 관리
- **다중 자녀 지원**: 한 계정에 여러 자녀 등록 가능
- **자녀별 설정**: 개별 자녀에 대한 맞춤 설정

### 푸시 알림 연동
- **FCM 토큰 관리**: Firebase 푸시 알림을 위한 토큰 관리
- **디바이스 등록**: 사용자 디바이스 정보 관리

## 기술 스택

- **Framework**: Spring Boot, Spring Data JPA
- **Security**: Spring Security, JWT
- **Database**: MySQL
- **Cache**: Redis
- **Push Notification**: Firebase Cloud Messaging (FCM)
- **Migration**: Flyway

## 아키텍처

### 주요 컴포넌트
- **Auth Domain**: 인증 및 권한 관리
- **User Domain**: 사용자 정보 관리
- **Child Domain**: 자녀 정보 관리
- **Security Config**: 보안 설정 및 JWT 처리

### 도메인 구조
```
user/
├── auth/              # 인증 도메인
│   ├── controller/
│   ├── service/
│   └── dto/
├── user/              # 사용자 도메인
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── entity/
├── child/             # 자녀 도메인
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── entity/
└── security/          # 보안 설정
```

## 실행 방법

```bash
# 애플리케이션 실행
./gradlew :services:user-service:bootRun

# 테스트 실행
./gradlew :services:user-service:test
```

## 환경 설정

필요한 환경 변수:
- `JWT_SECRET`: JWT 서명용 시크릿 키
- `JWT_EXPIRATION`: JWT 토큰 만료 시간
- `MYSQL_URL`: MySQL 데이터베이스 URL
- `REDIS_URL`: Redis 서버 URL
- `FIREBASE_CONFIG_PATH`: Firebase 설정 파일 경로

## API 엔드포인트

### 인증
- `POST /api/auth/login`: 로그인
- `POST /api/auth/logout`: 로그아웃
- `POST /api/auth/refresh`: 토큰 갱신
- `POST /api/auth/signup`: 회원가입

### 사용자 관리
- `GET /api/users/me`: 내 정보 조회
- `PUT /api/users/me`: 내 정보 수정
- `GET /api/users/{userId}`: 사용자 정보 조회
- `DELETE /api/users/me`: 회원 탈퇴

### 자녀 관리
- `POST /api/children`: 자녀 등록
- `GET /api/children`: 내 자녀 목록 조회
- `GET /api/children/{childId}`: 자녀 정보 조회
- `PUT /api/children/{childId}`: 자녀 정보 수정
- `DELETE /api/children/{childId}`: 자녀 정보 삭제

## 보안

### JWT 토큰
- **액세스 토큰**: 15분 유효기간
- **리프레시 토큰**: 30일 유효기간
- **토큰 블랙리스트**: 로그아웃된 토큰 관리

### 데이터 보안
- **비밀번호 암호화**: BCrypt 해싱
- **개인정보 보호**: 민감 정보 암호화
- **접근 제어**: 역할 기반 권한 관리

### API 보안
- **CORS 설정**: 허용된 도메인만 접근
- **Rate Limiting**: API 호출 제한
- **Input Validation**: 입력 데이터 검증

## 데이터베이스

### 주요 테이블
- **users**: 사용자 기본 정보
- **children**: 자녀 정보
- **user_roles**: 사용자 권한
- **refresh_tokens**: 리프레시 토큰 관리

### 관계
- User : Child = 1 : N (한 사용자가 여러 자녀 등록 가능)
- User : RefreshToken = 1 : N (멀티 디바이스 지원)

## 모니터링

- 사용자 등록/탈퇴 통계
- 로그인 성공/실패 률
- JWT 토큰 사용 패턴
- API 호출 통계