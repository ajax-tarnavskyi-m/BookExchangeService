package pet.project.app.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class User(
    val login: String?,
    val contact: String?,
    val bookWishList: MutableSet<Int>? = mutableSetOf(),
) {
    @Id
    lateinit var id: String
}
