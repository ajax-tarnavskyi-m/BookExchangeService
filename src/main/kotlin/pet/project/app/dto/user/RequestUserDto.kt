package pet.project.app.dto.user

data class RequestUserDto(
    val login: String,
    val contact: String?,
    val bookWishList: Set<String>?,
)
