package pet.project.app.model

data class Book (
    val id: Long,
    val title: String,
    val description: String,
    val yearOfPublishing: Int,
    val amazonPrice: Double,
    val amountAvailable : Int
)
