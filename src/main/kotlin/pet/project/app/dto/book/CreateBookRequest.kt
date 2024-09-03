package pet.project.app.dto.book

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import org.hibernate.validator.constraints.Range

data class CreateBookRequest(
    @field:NotEmpty(message = "Book title should not be empty")
    val title: String,
    val description: String?,
    @field:Range(min = 1600, max = 2024, message = "Publish year of book should be in valid range")
    val yearOfPublishing: Int,
    @Positive(message = "Book price should be grater than zero")
    val price: Double,
    @PositiveOrZero(message = "Book amount cant be negative")
    val amountAvailable: Int,
)
