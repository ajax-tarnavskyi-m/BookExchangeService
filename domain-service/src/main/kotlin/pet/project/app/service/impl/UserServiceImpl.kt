package pet.project.app.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pet.project.app.annotation.Profiling
import pet.project.app.model.domain.DomainUser
import pet.project.app.repository.BookRepository
import pet.project.app.repository.UserRepository
import pet.project.app.service.UserService
import pet.project.core.exception.BookNotFoundException
import pet.project.core.exception.UserNotFoundException
import pet.project.internal.input.reqreply.user.create.CreateUserRequest
import pet.project.internal.input.reqreply.user.update.UpdateUserRequest
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
            .switchIfEmpty(Mono.error { UserNotFoundException("Could not find user $userId during GET request") })

    override fun addBookToWishList(userId: String, bookId: String): Mono<Unit> {
        return bookRepository.existsById(bookId)
            .flatMap { isBookExist ->
                if (isBookExist) {
                    userRepository.addBookToWishList(userId, bookId)
                } else {
                    Mono.error { BookNotFoundException("Could not find book($bookId) for wishlist update") }
                }
            }.handle { matchCount, sink ->
                if (matchCount == 1L) {
                    sink.next(Unit)
                } else {
                    sink.error(UserNotFoundException("Could not find user($userId) for wishlist update"))
                }
            }
    }

    override fun update(userId: String, request: UpdateUserRequest): Mono<DomainUser> {
        return userRepository.update(userId, request)
            .switchIfEmpty(Mono.error { UserNotFoundException("Could not find user($userId) during UPDATE request") })
    }

    override fun delete(userId: String): Mono<Unit> {
        return userRepository.delete(userId)
            .doOnNext { deleteCount -> logIfUserNotFoundForDeletion(deleteCount, userId) }
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
