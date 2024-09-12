package pet.project.app.dto.book

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import pet.project.app.validation.ValidPublishingYearRange
import java.math.BigDecimal

data class CreateBookRequest(
    @field:NotEmpty(message = "Book title should not be empty")
    val title: String,
    val description: String?,
    @field:ValidPublishingYearRange(message = "Publishing year of the book should be within a valid range")
    val yearOfPublishing: Int,
    @field:Positive(message = "Book price should be greater than zero")
    val price: BigDecimal,
    @field:PositiveOrZero(message = "Book amount cannot be negative")
    val amountAvailable: Int,
)
