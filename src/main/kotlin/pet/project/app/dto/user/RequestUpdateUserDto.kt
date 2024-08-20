package pet.project.app.dto.user

data class RequestUpdateUserDto(
    val id: String,
    val login: String,
    val bookWishList: Set<String> = emptySet(),
)
