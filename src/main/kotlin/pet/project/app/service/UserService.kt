package pet.project.app.service

import org.springframework.stereotype.Service
import pet.project.app.model.User
import pet.project.app.repository.UserRepository

@Service
class UserService(val userRepository: UserRepository) {
    fun create(user: User): User {
        return userRepository.create(user);
    }

    fun getById(id: Long): User {
        return userRepository.getById(id)
    }

    fun update(id: Long, user: User): User {
        return userRepository.update(id, user)
    }

    fun delete(id: Long) : User {
        return userRepository.delete(id)
    }
}