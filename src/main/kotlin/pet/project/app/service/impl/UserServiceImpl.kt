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

@Profiling
@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val bookRepository: BookRepository,
) : UserService {

    override fun create(createUserRequest: CreateUserRequest): DomainUser = userRepository.insert(createUserRequest)

    override fun getById(userId: String): DomainUser =
        userRepository.findById(userId) ?: throw UserNotFoundException(userId, "GET request")

    override fun addBookToWishList(userId: String, bookId: String): Boolean {
        if (!bookRepository.existsById(bookId)) {
            throw BookNotFoundException(bookId, "adding book to wishlist of user with id=$userId")
        }
        val matchCount: Long = userRepository.addBookToWishList(userId, bookId)
        if (matchCount != 1L) {
            throw UserNotFoundException(userId, "adding book with id=$bookId into user wishlist")
        }
        return true
    }

    override fun update(userId: String, request: UpdateUserRequest): DomainUser {
        return userRepository.update(userId, request) ?: throw UserNotFoundException(userId, "UPDATE request")
    }

    override fun delete(userId: String) {
        val modifiedCount = userRepository.delete(userId)
        if (modifiedCount != 1L) {
            log.warn("Affected {} documents while trying to delete user with id={}", modifiedCount, userId)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(UserServiceImpl::class.java)
    }
}
