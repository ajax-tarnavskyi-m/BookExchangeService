package pet.project.gateway.dto.book

import java.math.BigDecimal

data class BookExternalResponse(
    val id: String,
    val title: String,
    val description: String?,
    val yearOfPublishing: Int,
    val price: BigDecimal?,
    val amountAvailable: Int,
)
