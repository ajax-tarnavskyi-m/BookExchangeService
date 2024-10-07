package pet.project.app.dto.user

import jakarta.validation.constraints.Email

data class UpdateUserRequest(
    val login: String?,
    @field:Email
    val email: String?,
    val bookWishList: Set<String>?,
)
