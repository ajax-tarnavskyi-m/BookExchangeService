package pet.project.app.dto.book

import pet.project.app.validation.NotZero

data class UpdateAmountRequest(
    @field:NotZero
    val delta : Int,
)
