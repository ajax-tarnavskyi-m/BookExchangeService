package pet.project.app.dto.book

import java.math.BigDecimal

data class ResponseBookDto(
    val id: String,
    val title: String,
    val description: String?,
    val yearOfPublishing: Int,
    val price: BigDecimal?,
    val amountAvailable: Int,
)
