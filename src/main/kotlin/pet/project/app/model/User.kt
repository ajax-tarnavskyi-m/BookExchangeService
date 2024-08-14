package pet.project.app.model

data class User(
    val id: Long,
    val login: String,
    val contact: String,
    val bookWishList: MutableSet<Int>
)