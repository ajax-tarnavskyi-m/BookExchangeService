package pet.project.app.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class User(
    val login: String?,
    val bookWishList: Set<String>? = mutableSetOf(),
) {
    @Id
    lateinit var id: String
}
