package pet.project.app.service

import mu.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pet.project.app.exception.UserNotFoundException
import pet.project.app.model.User
import pet.project.app.repository.UserRepository

@Service
class UserService(private val userRepository: UserRepository) {

    private val logger = KotlinLogging.logger {}

    fun create(user: User): User = userRepository.save(user)

    fun getById(userId: String): User =
        userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException(userId, "GET request")

    fun update(user: User): User {
        if (userRepository.existsById(user.id!!)) {
            return userRepository.save(user)
        }
        throw UserNotFoundException(user.id, "UPDATE request")
    }

    fun addBookToWishList(userId: String, bookId: String): Boolean {
        TODO("Implement")
    }

    fun delete(userId: String) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId)
        } else {
            logger.warn { "Attempting to delete absent user with id=$userId" }
        }
    }
}
