package pet.project.app.service

import pet.project.app.model.User

interface UserService {

    fun create(user: User): User
    fun getById(userId: String): User
    fun addBookToWishList(userId: String, bookId: String): User
    fun update(user: User): User
    fun delete(userId: String)
}
