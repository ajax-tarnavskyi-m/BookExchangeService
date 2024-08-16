package pet.project.app.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pet.project.app.model.User
import pet.project.app.repository.UserRepository

@Service
class UserService(private val userRepository: UserRepository) {
    fun create(user: User): User {
        return userRepository.save(user)
    }

    fun getById(userId: String): User {
        return userRepository.findByIdOrNull(userId) ?: throw Exception("No user with id = $userId")
    }

    fun update(userId: String, user: User): User {
        if (!userRepository.existsById(userId)) {
            throw Exception("No user with id = $userId, to update")
        }
        user.id = userId
        return userRepository.save(user)
    }

    fun addBookToWishList(userId: String, bookId: String): Boolean {
        TODO("Implement")
    }

    fun delete(userId: String) {
        if (!userRepository.existsById(userId)) {
            throw Exception("No user with id = $userId, to delete")
        }
        userRepository.deleteById(userId)
    }
}
