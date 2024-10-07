package pet.project.app.repository

import pet.project.app.dto.user.CreateUserRequest
import pet.project.app.dto.user.UpdateUserRequest
import pet.project.app.dto.user.UserNotificationDetails
import pet.project.app.model.domain.DomainUser

interface UserRepository {
    fun insert(createUserRequest: CreateUserRequest): DomainUser
    fun findById(id: String): DomainUser?
    fun findAllBookSubscribers(bookId: String): List<UserNotificationDetails>
    fun findAllBookListSubscribers(booksIds: List<String>): List<UserNotificationDetails>
    fun addBookToWishList(userId: String, bookId: String): Long
    fun update(userId: String, request: UpdateUserRequest): DomainUser?
    fun delete(id: String): Long
}
