package pet.project.gateway.dto.book

import pet.project.gateway.validation.NotZero
import pet.project.gateway.validation.ValidObjectId

data class UpdateAmountExternalRequest(
    @field:ValidObjectId
    val bookId: String,
    @field:NotZero(message = "Delta value must not be zero")
    val delta: Int,
)
