package pet.project.app.controller.dto.user


data class ResponseUserDto (
    val id: String?,
    val login: String?,
    val contact: String?,
    val bookWishList: MutableSet<Int>?
)

