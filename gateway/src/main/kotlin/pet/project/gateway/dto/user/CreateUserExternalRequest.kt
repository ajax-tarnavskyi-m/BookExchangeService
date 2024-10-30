package pet.project.gateway.dto.user

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class CreateUserExternalRequest(
    @field:NotBlank(message = "User login must not be blank")
    val login: String,
    @field:Email
    val email: String,
    val bookWishList: Set<String> = emptySet(),
)
