package pet.project.app.service

import pet.project.app.dto.user.CreateUserRequest
import pet.project.app.dto.user.UpdateUserRequest
import pet.project.app.model.domain.DomainUser

interface UserService {

    fun create(createUserRequest: CreateUserRequest): DomainUser

    fun getById(userId: String): DomainUser

    fun addBookToWishList(userId: String, bookId: String): Boolean

    fun update(userId: String, request: UpdateUserRequest): DomainUser

    fun delete(userId: String)
}
