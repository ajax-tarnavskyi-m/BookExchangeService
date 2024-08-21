package pet.project.app.exception

class UserNotFoundException(
    userId: String,
    actionDescription: String,
) : RuntimeException("User with id=$userId does not found while trying to perform $actionDescription")
