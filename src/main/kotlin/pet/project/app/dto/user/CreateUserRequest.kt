package pet.project.app.dto.user

import jakarta.validation.constraints.NotBlank

data class CreateUserRequest(
    @field:NotBlank
    val login: String,
    val bookWishList: Set<String> = emptySet(),
)
