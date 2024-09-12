package pet.project.app.exception

class UserNotFoundException(
    userId: String,
    actionDescription: String,
) : RuntimeException("User with id=$userId was not found during $actionDescription")
