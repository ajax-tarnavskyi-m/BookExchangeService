package pet.project.app.dto.user

import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import pet.project.app.model.User

@Component
class UserMapper {

    fun toModel(request: CreateUserRequest) = User(
        login = request.login,
        email = request.email,
        bookWishList = request.bookWishList.map { ObjectId(it) }.toSet()
    )

    fun toModel(request: UpdateUserRequest) = User(
        ObjectId(request.id),
        request.login,
        request.email,
        request.bookWishList.map { ObjectId(it) }.toSet(),
    )

    fun toDto(user: User) = ResponseUserDto(
        user.id!!.toHexString(),
        user.login,
        user.email,
        user.bookWishList.map { it.toHexString() }.toSet(),
    )
}
