package pet.project.app.dto.user

import jakarta.validation.constraints.NotBlank

data class CreateUserRequest(
    @NotBlank
    val login: String,
    val bookWishList: Set<String> = emptySet(),
)
