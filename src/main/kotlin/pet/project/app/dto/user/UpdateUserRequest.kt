package pet.project.app.dto.user

import jakarta.validation.constraints.NotBlank

data class UpdateUserRequest(
    @NotBlank
    val id: String,
    @NotBlank
    val login: String,
    val bookWishList: Set<String> = emptySet(),
)
