package pet.project.app.model.domain

data class DomainUser(
    val id: String,
    val login: String,
    val email: String,
    val bookWishList: Set<String>,
)
