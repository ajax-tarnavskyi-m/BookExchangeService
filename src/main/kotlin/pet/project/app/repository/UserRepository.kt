package pet.project.app.repository

import pet.project.app.dto.user.UserNotificationDetails
import pet.project.app.model.User

interface UserRepository {
    fun insert(user: User): User
    fun findById(id: String): User?
    fun delete(id: String): Long
    fun update(user: User): Long
    fun findAllBookSubscribers(bookId: String): List<UserNotificationDetails>
    fun findAllBookListSubscribers(booksIds: List<String>): List<UserNotificationDetails>
    fun addBookToWishList(userId: String, bookId: String): Long
}
