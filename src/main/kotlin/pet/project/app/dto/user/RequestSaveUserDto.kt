package pet.project.app.dto.user

data class RequestSaveUserDto(
    val login: String,
    val bookWishList: Set<String> = emptySet(),
)
