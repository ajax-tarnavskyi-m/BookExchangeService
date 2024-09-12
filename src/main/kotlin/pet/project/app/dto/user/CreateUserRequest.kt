package pet.project.app.dto.user

import jakarta.validation.constraints.NotBlank

data class CreateUserRequest(
    @field:NotBlank(message = "User login must not be blank")
    val login: String,
    val bookWishList: Set<String> = emptySet(),
)
