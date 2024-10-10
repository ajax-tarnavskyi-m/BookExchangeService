package pet.project.app.model.mongo

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal

@TypeAlias("Book")
@Document(collection = MongoBook.COLLECTION_NAME)
data class MongoBook(
    @Id
    val id: ObjectId? = null,
    val title: String? = null,
    val description: String? = null,
    val yearOfPublishing: Int? = null,
    val price: BigDecimal? = null,
    val amountAvailable: Int? = null,
    val shouldBeNotified: Boolean? = null
) {
    companion object {
        const val COLLECTION_NAME = "book"
    }
}
