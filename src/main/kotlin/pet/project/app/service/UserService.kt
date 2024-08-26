package pet.project.app.service

import mu.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pet.project.app.annotation.Profiling
import pet.project.app.exception.BookNotFoundException
import pet.project.app.exception.UserNotFoundException
import pet.project.app.model.User
import pet.project.app.repository.BookRepository
import pet.project.app.repository.UserRepository

@Profiling
@Service
class UserService(
    private val userRepository: UserRepository,
    private val bookRepository: BookRepository,
) {

    fun create(user: User): User = this.userRepository.save(user)

    fun getById(userId: String): User =
        userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException(userId, "GET request")

    fun update(user: User): User {
        if (this.userRepository.existsById(user.id!!.toHexString())) {
            return this.userRepository.save(user)
        }
        throw UserNotFoundException(user.id.toHexString(), "UPDATE request")
    }

    fun addBookToWishList(userId: String, bookId: String): User {
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException(
            userId, "adding book with id=$bookId into user wishlist"
        )
        if (!bookRepository.existsById(bookId)) {
            throw BookNotFoundException(bookId, "adding book to wishlist of user with id=$userId")
        }
        val updatedWishList = user.bookWishList.plus(bookId)
        val updatedUser = user.copy(bookWishList = updatedWishList)
        return this.userRepository.save(updatedUser)
    }

    fun delete(userId: String) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId)
        } else {
            logger.warn { "Attempting to delete absent user with id=$userId" }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
