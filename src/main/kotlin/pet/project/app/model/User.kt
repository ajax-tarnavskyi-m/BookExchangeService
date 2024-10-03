package pet.project.app.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = User.COLLECTION_NAME)
data class User(
    @Id
    val id: ObjectId? = null,
    val login: String,
    val email: String,
    val bookWishList: Set<ObjectId> = emptySet(),
) {
    companion object {
        const val COLLECTION_NAME = "user"
    }
}
