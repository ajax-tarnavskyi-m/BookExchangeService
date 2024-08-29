package pet.project.app.dto.book

data class UpdateAmountRequest(
    val bookId : String,
    val delta : Int,
)
