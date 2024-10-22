package pet.project.app.dto.book

import jakarta.validation.constraints.Positive
import pet.project.core.exception.validation.ValidPublishingYearRange
import java.math.BigDecimal

data class UpdateBookRequest(
    val title: String?,
    val description: String?,
    @field:ValidPublishingYearRange(message = "Publishing year of the book should be within a valid range")
    val yearOfPublishing: Int?,
    @field:Positive(message = "Book price should be greater than zero")
    val price: BigDecimal?,
)
