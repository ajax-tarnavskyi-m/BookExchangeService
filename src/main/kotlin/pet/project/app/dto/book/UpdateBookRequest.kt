package pet.project.app.dto.book

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import org.hibernate.validator.constraints.Range
import pet.project.app.validation.ValidObjectId

data class UpdateBookRequest(
    @field:ValidObjectId
    val id: String,
    @field:NotBlank
    val title: String,
    val description: String?,
    @field:Range(min = 1600, max = 2024)
    val yearOfPublishing: Int,
    @field:Positive
    val price: Double,
    @field:PositiveOrZero
    val amountAvailable: Int,
)
