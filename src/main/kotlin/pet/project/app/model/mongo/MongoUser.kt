package pet.project.app.model.mongo

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@TypeAlias("User")
@Document(collection = MongoUser.COLLECTION_NAME)
data class MongoUser(
    @Id
    val id: ObjectId? = null,
    val login: String? = null,
    val email: String? = null,
    val bookWishList: Set<ObjectId>? = null,
) {
    companion object {
        const val COLLECTION_NAME = "user"
    }
}
