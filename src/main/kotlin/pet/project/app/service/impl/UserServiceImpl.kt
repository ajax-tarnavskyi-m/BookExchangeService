package pet.project.app.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pet.project.app.annotation.Profiling
import pet.project.app.exception.BookNotFoundException
import pet.project.app.exception.UserNotFoundException
import pet.project.app.model.User
import pet.project.app.repository.BookRepository
import pet.project.app.repository.UserRepository
import pet.project.app.service.UserService

@Profiling
@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val bookRepository: BookRepository,
) : UserService {

    override fun create(user: User): User = userRepository.insert(user)

    override fun getById(userId: String): User =
        userRepository.findById(userId) ?: throw UserNotFoundException(userId, "GET request")

    override fun update(user: User): User {
        val updatedDocumentsCount = userRepository.update(user)
        if (updatedDocumentsCount != 1L) {
            log.warn("Affected {} documents while trying to update user with id={}", updatedDocumentsCount, user.id)
        }
        return user
    }

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
