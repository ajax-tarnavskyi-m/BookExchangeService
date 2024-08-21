package pet.project.app.dto.user

import pet.project.app.model.User

object UserMapper {

    fun CreateUserRequest.toModel() = User(
        login = login,
        bookWishList = bookWishList
    )

    fun UpdateUserRequest.toModel() = User(
        id,
        login,
        bookWishList
    )

    fun User.toDto() = ResponseUserDto(
        id!!,
        login ?: "",
        bookWishList,
    )

}
