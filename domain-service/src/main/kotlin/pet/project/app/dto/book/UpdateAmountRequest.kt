package pet.project.app.dto.book

import pet.project.app.validation.NotZero
import pet.project.app.validation.ValidObjectId

data class UpdateAmountRequest(
    @field:ValidObjectId
    val bookId: String,
    @field:NotZero(message = "Delta value must not be zero")
    val delta: Int,
)
