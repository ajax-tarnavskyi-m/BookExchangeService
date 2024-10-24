package pet.project.gateway.dto.user

data class UserExternalResponse(
    val id: String,
    val login: String,
    val email: String,
    val bookWishList: Set<String>,
)
