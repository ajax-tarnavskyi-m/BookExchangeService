package pet.project.app.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class User(
    @Id
    val id: ObjectId = ObjectId.get(),
    val login: String?,
    val bookWishList: Set<String> = emptySet(),
)
