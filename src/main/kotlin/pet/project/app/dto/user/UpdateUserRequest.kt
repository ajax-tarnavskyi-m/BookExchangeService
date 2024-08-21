package pet.project.app.dto.user

data class UpdateUserRequest(
    val id: String,
    val login: String,
    val bookWishList: Set<String> = emptySet(),
)
