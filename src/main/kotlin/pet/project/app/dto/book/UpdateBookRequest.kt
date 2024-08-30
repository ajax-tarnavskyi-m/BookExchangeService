package pet.project.app.dto.book

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import pet.project.app.validation.ValidOpenId
import pet.project.app.validation.YearBeforeCurrent

data class UpdateBookRequest(
    @ValidOpenId
    val id: String,
    @NotBlank
    val title: String,
    val description: String?,
    @Min(value = 1600)
    @YearBeforeCurrent
    val yearOfPublishing: Int,
    @Positive
    val price: Double,
    @PositiveOrZero
    val amountAvailable: Int,
)
