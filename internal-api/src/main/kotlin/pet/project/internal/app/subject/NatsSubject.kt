package pet.project.internal.app.subject

object NatsSubject {
    object User {
        const val PREFIX = "user"
        const val CREATE = "$PREFIX.create"
        const val FIND_BY_ID = "$PREFIX.find_by_id"
        const val UPDATE = "$PREFIX.update"
        const val ADD_BOOK_TO_WISH_LIST = "$PREFIX.add_book_to_wish_list"
        const val DELETE = "$PREFIX.delete"
    }
}
