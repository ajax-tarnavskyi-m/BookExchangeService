package pet.project.app.dto.book

data class CreateBookRequest(
    val title: String,
    val description: String?,
    val yearOfPublishing: Int,
    val price: Double,
    val amountAvailable: Int,
)
