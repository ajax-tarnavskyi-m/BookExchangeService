package pet.project.app.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal

@Document
data class Book(
    @Id
    val id: ObjectId? = null,
    val title: String?,
    val description: String?,
    val yearOfPublishing: Int?,
    val price: BigDecimal?,
    val amountAvailable: Int?,
)
