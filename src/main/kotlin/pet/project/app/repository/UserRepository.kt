package pet.project.app.repository

import pet.project.app.dto.user.CreateUserRequest
import pet.project.app.dto.user.UpdateUserRequest
import pet.project.app.dto.user.UserNotificationDetails
import pet.project.app.model.domain.DomainUser
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface UserRepository {
    fun insert(createUserRequest: CreateUserRequest): Mono<DomainUser>
    fun findById(id: String): Mono<DomainUser>
    fun findAllSubscribersOf(booksIds: List<String>): Flux<UserNotificationDetails>
    fun addBookToWishList(userId: String, bookId: String): Mono<Long>
    fun update(userId: String, request: UpdateUserRequest): Mono<DomainUser>
    fun delete(id: String): Mono<Long>
}
