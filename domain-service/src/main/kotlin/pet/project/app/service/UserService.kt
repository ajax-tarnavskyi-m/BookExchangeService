package pet.project.app.service

import pet.project.app.model.domain.DomainUser
import pet.project.internal.input.reqreply.user.create.CreateUserRequest
import pet.project.internal.input.reqreply.user.update.UpdateUserRequest
import reactor.core.publisher.Mono

interface UserService {

    fun create(createUserRequest: CreateUserRequest): Mono<DomainUser>

    fun getById(userId: String): Mono<DomainUser>

    fun addBookToWishList(userId: String, bookId: String): Mono<Unit>

    fun update(userId: String, request: UpdateUserRequest): Mono<DomainUser>

    fun delete(userId: String): Mono<Unit>
}
