package pet.project.app.dto.user

import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import pet.project.app.model.User

@Component
class UserMapper {

    fun toModel(request: CreateUserRequest) = User(
        login = request.login,
        bookWishList = request.bookWishList,
    )

    fun toModel(request: UpdateUserRequest) = User(
        ObjectId(request.id),
        request.login,
        request.bookWishList,
    )

    fun toDto(user: User) = ResponseUserDto(
        user.id!!.toHexString(),
        user.login ?: "",
        user.bookWishList,
    )
}
