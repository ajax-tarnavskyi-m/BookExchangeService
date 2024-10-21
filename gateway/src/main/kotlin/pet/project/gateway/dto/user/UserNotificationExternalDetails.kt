package pet.project.gateway.dto.user

data class UserNotificationExternalDetails(
    val login: String,
    val email: String,
    val bookTitles: Set<String>,
)
