package pet.project.app.repository

import pet.project.app.dto.user.UserNotificationDetails
import pet.project.app.model.domain.DomainUser
import pet.project.internal.input.reqreply.user.CreateUserRequest
import pet.project.internal.input.reqreply.user.UpdateUserRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface UserRepository {
    fun insert(createUserRequest: CreateUserRequest): Mono<DomainUser>
    fun findById(id: String): Mono<DomainUser>
    fun findAllSubscribersOf(booksIds: Collection<String>): Flux<UserNotificationDetails>
    fun addBookToWishList(userId: String, bookId: String): Mono<Long>
    fun update(userId: String, request: UpdateUserRequest): Mono<DomainUser>
    fun delete(id: String): Mono<Long>
}
