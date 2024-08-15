package pet.project.app.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pet.project.app.repository.model.User
import pet.project.app.repository.UserRepository

@Service
class UserService(val userRepository: UserRepository) {
    fun create(user: User): User {
        return userRepository.save(user);
    }

    fun getById(id: Long): User {
        return userRepository.findByIdOrNull(id) ?: throw Exception("No user with id = $id")
    }

    fun update(id: Long, user: User): User {
        if (!userRepository.existsById(id)) {
            throw Exception("No user with id = $id, to update")
        }
        return userRepository.save(user)
    }

    fun addBookToWishList(userId: Long, bookId: Long): Boolean {
        TODO("Implement")
    }

    fun delete(id: Long) {
        if (!userRepository.existsById(id)) {
            throw Exception("No user with id = $id, to delete")
        }
        userRepository.deleteById(id);
    }
}