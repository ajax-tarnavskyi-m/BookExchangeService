package pet.project.app.dto.user

data class UserNotificationDetails(
    val login: String,
    val email: String,
    val bookTitles: Set<String>,
)
