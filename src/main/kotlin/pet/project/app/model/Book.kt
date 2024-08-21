package pet.project.app.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Book(
    @Id
    val id: ObjectId = ObjectId.get(),
    val title: String?,
    val description: String?,
    val yearOfPublishing: Int?,
    val price: Double?,
    val amountAvailable: Int?,
)
