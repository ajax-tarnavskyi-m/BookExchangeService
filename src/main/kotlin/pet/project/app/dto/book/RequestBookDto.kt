package pet.project.app.dto.book

data class RequestBookDto(
    val title: String,
    val description: String?,
    val yearOfPublishing: Int,
    val amazonPrice: Double,
    val amountAvailable: Int,
)
