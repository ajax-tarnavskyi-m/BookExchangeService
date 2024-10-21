package pet.project.gateway.dto.book

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal

data class CreateBookExternalRequest(
    @field:NotEmpty(message = "Book title should not be empty")
    val title: String,
    val description: String?,
    val yearOfPublishing: Int,
    @field:Positive(message = "Book price should be greater than zero")
    val price: BigDecimal,
    @field:PositiveOrZero(message = "Book amount cannot be negative")
    val amountAvailable: Int,
)
