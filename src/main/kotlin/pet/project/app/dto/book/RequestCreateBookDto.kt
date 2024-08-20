package pet.project.app.dto.book

data class RequestCreateBookDto(
    val title: String,
    val description: String?,
    val yearOfPublishing: Int,
    val price: Double,
    val amountAvailable: Int,
)
