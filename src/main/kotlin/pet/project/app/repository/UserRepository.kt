package pet.project.app.repository

import org.springframework.stereotype.Repository
import pet.project.app.model.User

@Repository
class UserRepository {
    val mockUser = User(1, "Login", "Contact", mutableSetOf(1, 2, 3))

    fun create(user: User): User {
        return mockUser
    }

    fun getById(id: Long): User {
        return mockUser
    }

    fun update(id: Long, user: User): User {
        return mockUser
    }

    fun delete(id: Long): User {
        return mockUser
    }

    fun findAllWhereWishListContainsWithId(bookId: Long) : List<User> {
        return mutableListOf(mockUser)
    }
}