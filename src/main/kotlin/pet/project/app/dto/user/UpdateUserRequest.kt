package pet.project.app.dto.user

import jakarta.validation.constraints.NotBlank
import pet.project.app.validation.ValidObjectId

data class UpdateUserRequest(
    @field:ValidObjectId
    val id: String,
    @field:NotBlank
    val login: String,
    val bookWishList: Set<String> = emptySet(),
)
