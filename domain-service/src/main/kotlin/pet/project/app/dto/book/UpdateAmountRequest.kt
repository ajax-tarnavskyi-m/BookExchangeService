package pet.project.app.dto.book

import pet.project.core.exception.validation.NotZero
import pet.project.core.exception.validation.ValidObjectId


data class UpdateAmountRequest(
    @field:ValidObjectId
    val bookId: String,
    @field:NotZero(message = "Delta value must not be zero")
    val delta: Int,
)
