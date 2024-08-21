package pet.project.app.dto.user

data class CreateUserRequest(
    val login: String,
    val bookWishList: Set<String> = emptySet(),
)
