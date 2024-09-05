package pet.project.app.dto.book

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import org.hibernate.validator.constraints.Range
import pet.project.app.validation.ValidObjectId
import pet.project.app.validation.ValidPublishingYearRange
import java.math.BigDecimal

data class UpdateBookRequest(
    @field:ValidObjectId(message = "The provided ID must be a valid ObjectId hex String")
    val id: String,
    @field:NotBlank(message = "Book title must not be blank")
    val title: String,
    val description: String?,
    @field:ValidPublishingYearRange(message = "Publishing year of the book should be within a valid range")
    val yearOfPublishing: Int,
    @field:Positive(message = "Book price must be greater than zero")
    val price: BigDecimal,
    @field:PositiveOrZero(message = "Book amount cannot be negative")
    val amountAvailable: Int,
)
