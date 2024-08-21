package pet.project.app.dto.user

import org.bson.types.ObjectId
import pet.project.app.model.User

object UserMapper {

    fun CreateUserRequest.toModel() = User(
        login = login,
        bookWishList = bookWishList
    )

    fun UpdateUserRequest.toModel() = User(
        ObjectId(id),
        login,
        bookWishList
    )

    fun User.toDto() = ResponseUserDto(
        id.toHexString(),
        login ?: "",
        bookWishList,
    )

}
