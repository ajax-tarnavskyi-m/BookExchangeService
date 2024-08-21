package pet.project.app.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class User(
    @Id
    val id: String? = null,
    val login: String?,
    val bookWishList: Set<String> = emptySet(),
)
