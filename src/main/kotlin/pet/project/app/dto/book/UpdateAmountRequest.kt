package pet.project.app.dto.book

import pet.project.app.validation.NotZero

data class UpdateAmountRequest(
    @field:NotZero(message = "Delta value must not be zero")
    val delta: Int,
)
