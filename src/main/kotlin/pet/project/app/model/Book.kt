package pet.project.app.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Book(
    val title: String?,
    val description: String?,
    val yearOfPublishing: Int?,
    val amazonPrice: Double?,
    val amountAvailable: Int?,
) {
    @Id
    lateinit var id: String
}
