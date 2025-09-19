# Redis Module (in shared/common)

AIVA 백엔드 시스템의 공통 Redis 모듈입니다. `shared/common` 내부에 위치하여 모든 마이크로서비스에서 Redis를 일관성 있게 사용할 수 있도록 표준화된 설정, 엔티티, 서비스를 제공합니다.

## 위치 및 구조

```
shared/common/src/main/kotlin/com/aiva/common/
└── redis/
    ├── config/           # Redis 설정 및 AutoConfiguration
    ├── entity/           # Redis 엔티티 (@RedisHash)
    ├── repository/       # Spring Data Redis Repository
    └── service/          # Redis 서비스 계층
```

## 주요 기능

### 통합 Redis 설정
- **자동 설정**: Spring Boot AutoConfiguration을 통한 자동 설정
- **JSON 직렬화**: Jackson 기반 JSON 직렬화 설정
- **Kotlin 지원**: Kotlin 모듈 및 JSR310 (LocalDateTime) 지원

### 표준화된 Redis 엔티티
- **AuthSession**: 사용자 인증 세션 관리 (`@RedisHash`)
- **ChatSession**: AI 채팅 세션 및 컨텍스트 관리
- **CommunityPostCache**: 커뮤니티 게시글 캐시
- **NotificationQueue**: 알림 큐 관리
- **SubscriptionCache**: 구독 정보 캐시

### 서비스 계층
- **RedisAuthService**: 인증 세션 관리 서비스
- **RedisCommunityService**: 커뮤니티 캐시 서비스
- **RedisNotificationService**: 알림 큐 서비스

## 사용 방법

### 1. 의존성 추가

Redis 모듈은 `shared/common`에 포함되어 있으므로 별도 의존성 추가가 필요 없습니다:

```kotlin
// build.gradle.kts
dependencies {
    implementation(project(":shared:common"))  // Redis 모듈 포함
    implementation(project(":shared:security"))
}
```

### 2. 자동 설정 활성화

Spring Boot 애플리케이션에서 자동으로 설정됩니다.

```kotlin
@SpringBootApplication
class YourServiceApplication
```

### 3. 서비스 사용

```kotlin
@Service
class YourService(
    private val redisAuthService: RedisAuthService,
    private val redisCommunityService: RedisCommunityService
) {
    
    fun createUserSession(userId: UUID, refreshToken: String): AuthSession {
        return redisAuthService.createSession(
            userId = userId,
            refreshToken = refreshToken,
            deviceInfo = "Mobile App",
            ipAddress = "192.168.1.1"
        )
    }
    
    fun cachePost(postId: String, title: String, content: String) {
        redisCommunityService.cachePost(
            postId = postId,
            title = title,
            content = content,
            authorId = userId,
            authorNickname = "User",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
}
```

## Import 경로

모든 Redis 관련 클래스는 `com.aiva.common.redis` 패키지 하위에 있습니다:

```kotlin
import com.aiva.common.redis.entity.AuthSession
import com.aiva.common.redis.service.RedisAuthService
import com.aiva.common.redis.repository.AuthSessionRepository
```

## 왜 shared/common 안에?

1. **논리적 일관성**: Redis는 공통 인프라 컴포넌트로 common 모듈의 일부
2. **의존성 단순화**: 별도 모듈 없이 common 의존성만으로 사용 가능
3. **모듈 관리**: 과도한 모듈 분리를 피하고 관련 기능 통합

## 확장 방법

### 새로운 Redis 엔티티 추가

1. `shared/common/src/main/kotlin/com/aiva/common/redis/entity`에 엔티티 생성
2. `shared/common/src/main/kotlin/com/aiva/common/redis/repository`에 Repository 생성
3. `shared/common/src/main/kotlin/com/aiva/common/redis/service`에 서비스 생성

```kotlin
@RedisHash("your:domain")
data class YourEntity(
    @Id val id: String,
    @Indexed val userId: UUID,
    @TimeToLive val ttl: Long = 3600L // 1 hour
)

@Repository
interface YourEntityRepository : CrudRepository<YourEntity, String>

@Service
class YourRedisService(private val repository: YourEntityRepository)
```

## 기존 코드 마이그레이션

기존에 `com.aiva.redis.*`를 사용하던 코드는 `com.aiva.common.redis.*`로 변경:

```kotlin
// Before
import com.aiva.redis.entity.AuthSession
import com.aiva.redis.service.RedisAuthService

// After  
import com.aiva.common.redis.entity.AuthSession
import com.aiva.common.redis.service.RedisAuthService
```