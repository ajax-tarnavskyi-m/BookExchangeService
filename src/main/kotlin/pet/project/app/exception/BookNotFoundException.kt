package pet.project.app.exception

class BookNotFoundException(
    bookId: String,
    actionDescription: String,
) : RuntimeException("Book with id=$bookId was not found during $actionDescription")
