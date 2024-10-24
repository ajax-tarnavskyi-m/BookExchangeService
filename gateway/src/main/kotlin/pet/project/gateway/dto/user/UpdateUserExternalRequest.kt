package pet.project.gateway.dto.user

import jakarta.validation.constraints.Email

data class UpdateUserExternalRequest(
    val login: String?,
    @field:Email
    val email: String?,
    val bookWishList: Set<String>?,
)
