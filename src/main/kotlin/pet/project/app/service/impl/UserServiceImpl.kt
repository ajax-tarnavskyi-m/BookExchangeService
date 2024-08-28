package pet.project.app.service.impl

import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
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

    override fun create(user: User): User = userRepository.save(user)

    override fun getById(userId: String): User =
        userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException(userId, "GET request")

    override fun update(user: User): User {
        if (this.userRepository.existsById(user.id!!.toHexString())) {
            return userRepository.save(user)
        }
        throw UserNotFoundException(user.id.toHexString(), "UPDATE request")
    }

    override fun addBookToWishList(userId: String, bookId: String): User {
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException(
            userId, "adding book with id=$bookId into user wishlist"
        )
        if (!bookRepository.existsById(bookId)) {
            throw BookNotFoundException(bookId, "adding book to wishlist of user with id=$userId")
        }
        val updatedWishList = user.bookWishList.plus(bookId)
        val updatedUser = user.copy(bookWishList = updatedWishList)
        return userRepository.save(updatedUser)
    }

    override fun delete(userId: String) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId)
        } else {
            log.warn("Attempting to delete absent user with id={}", userId)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(UserServiceImpl::class.java)
    }
}
