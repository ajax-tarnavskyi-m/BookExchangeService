package pet.project.app.service

import pet.project.app.dto.user.CreateUserRequest
import pet.project.app.dto.user.UpdateUserRequest
import pet.project.app.model.domain.DomainUser
import reactor.core.publisher.Mono

interface UserService {

    fun create(createUserRequest: CreateUserRequest): Mono<DomainUser>

    fun getById(userId: String): Mono<DomainUser>

    fun addBookToWishList(userId: String, bookId: String): Mono<Unit>

    fun update(userId: String, request: UpdateUserRequest): Mono<DomainUser>

    fun delete(userId: String) : Mono<Unit>
}
