package pet.project.app.model.domain

import java.math.BigDecimal

data class DomainBook(
    val id: String,
    val title: String,
    val description: String,
    val yearOfPublishing: Int,
    val price: BigDecimal,
    val amountAvailable: Int,
)
