package pet.project.app.controller.dto.user

data class RequestUserDto(
    val login: String,
    val contact: String?,
    val bookWishList: MutableSet<Int>?
)