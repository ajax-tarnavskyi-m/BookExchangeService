package pet.project.app.dto.user

data class ResponseUserDto(
    val id: String,
    val login: String,
    val email: String,
    val bookWishList: Set<String>,
)
