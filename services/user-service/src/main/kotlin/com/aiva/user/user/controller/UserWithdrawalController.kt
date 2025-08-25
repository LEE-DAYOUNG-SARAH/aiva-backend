import com.aiva.common.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserWithdrawalController(
    private val userWithdrawalService: UserWithdrawalService
) {
    @DeleteMapping("/me")
    fun withdraw(
        @RequestHeader("X-User-Id") userId: String,
        @Valid @RequestBody request: UserWithdrawalRequest
    ): ApiResponse<UserWithdrawalResponse> = ApiResponse.success(
        userWithdrawalService.withdraw(userId, request)
    )
}