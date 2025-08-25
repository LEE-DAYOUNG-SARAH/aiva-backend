import com.aiva.security.exception.UnauthorizedException
import com.aiva.user.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class UserWithdrawalService(
    private val userRepository: UserRepository
) {
    fun withdraw(userIdString: String, request: UserWithdrawalRequest): UserWithdrawalResponse {
        val userId = UUID.fromString(userIdString)
        val user = userRepository.findById(userId)
            .orElseThrow { UnauthorizedException("사용자를 찾을 수 없습니다.") }

        if(user.isProUser()) {
            throw IllegalStateException("프로 유저는 탈퇴할 수 없습니다.")
        }

        user.withdraw(request.reason)

        // TODO. Redis 캐시 삭제

        return UserWithdrawalResponse.from(user)
    }
}