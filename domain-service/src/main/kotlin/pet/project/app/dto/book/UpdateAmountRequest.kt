package pet.project.app.dto.book

import pet.project.core.validation.NotZero
import pet.project.core.validation.ValidObjectId

data class UpdateAmountRequest(
    @field:ValidObjectId
    val bookId: String,
    @field:NotZero(message = "Delta value must not be zero")
    val delta: Int,
)
