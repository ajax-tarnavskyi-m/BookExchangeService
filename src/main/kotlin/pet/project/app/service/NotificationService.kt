package pet.project.app.service

interface NotificationService {
    fun notifySubscribedUsers(bookId: String)

    fun notifySubscribedUsers(bookIds: List<String>)
}
