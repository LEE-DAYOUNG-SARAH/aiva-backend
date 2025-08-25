import com.aiva.user.user.entity.User
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class UserWithdrawalRequest(
    @field:NotBlank(message = "탈퇴 사유는 필수입니다.")
    @field:Size(max = 50, message = "탈퇴 사유는 최대 50자 이하여야 합니다.")
    val reason: String,
)

data class UserWithdrawalResponse(
    val withdrawalReason: String,
    val deletedAt: LocalDateTime
) {
    companion object {
        fun from(user: User) = UserWithdrawalResponse(
            withdrawalReason = user.withdrawalReason.orEmpty(),
            deletedAt = user.deletedAt ?: LocalDateTime.now()
        )
    }
}