package pet.project.app.dto.user

import jakarta.validation.constraints.NotBlank
import pet.project.app.validation.ValidObjectId

data class UpdateUserRequest(
    @field:ValidObjectId(message = "The provided ID must be a valid ObjectId")
    val id: String,
    @field:NotBlank(message = "User login must not be blank")
    val login: String,
    val bookWishList: Set<String> = emptySet(),
)
