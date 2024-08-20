package pet.project.app.exception

class BookNotFoundException(
    bookId: String,
    actionDescription: String
) : RuntimeException("Book with id=$bookId does not found while trying to perform $actionDescription")
