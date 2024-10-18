package pet.project.app.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pet.project.app.annotation.Profiling
import pet.project.app.dto.user.CreateUserRequest
import pet.project.app.dto.user.UpdateUserRequest
import pet.project.app.exception.BookNotFoundException
import pet.project.app.exception.UserNotFoundException
import pet.project.app.model.domain.DomainUser
import pet.project.app.repository.BookRepository
import pet.project.app.repository.UserRepository
import pet.project.app.service.UserService
import reactor.core.publisher.Mono

@Profiling
@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val bookRepository: BookRepository,
) : UserService {

    override fun create(createUserRequest: CreateUserRequest): Mono<DomainUser> =
        userRepository.insert(createUserRequest)

    override fun getById(userId: String): Mono<DomainUser> =
        userRepository.findById(userId)
            .switchIfEmpty(Mono.error { UserNotFoundException(userId, "GET request") })

    override fun addBookToWishList(userId: String, bookId: String): Mono<Unit> {
        return bookRepository.existsById(bookId)
            .flatMap { isBookExist ->
                if (isBookExist) {
                    userRepository.addBookToWishList(userId, bookId)
                } else {
                    Mono.error { BookNotFoundException(bookId, "adding book to users (id=$userId) wishlist") }
                }
            }.handle { matchCount, sink ->
                if (matchCount == 1L) {
                    sink.next(Unit)
                } else {
                    sink.error(UserNotFoundException(userId, "adding book with id=$bookId into user wishlist"))
                }
            }
    }

    override fun update(userId: String, request: UpdateUserRequest): Mono<DomainUser> {
        return userRepository.update(userId, request)
            .switchIfEmpty(Mono.error { UserNotFoundException(userId, "UPDATE request") })
    }

    override fun delete(userId: String): Mono<Unit> {
        return userRepository.delete(userId)
            .doOnNext { deleteCount -> logIfUserNotFoundForDeletion(deleteCount, userId)}
            .thenReturn(Unit)
    }

    private fun logIfUserNotFoundForDeletion(deleteCount: Long, userId: String) {
        if (deleteCount != 1L) {
            log.warn("Affected {} documents while trying to delete user with id={}", deleteCount, userId)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(UserServiceImpl::class.java)
    }
}
