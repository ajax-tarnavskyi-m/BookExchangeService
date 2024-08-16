package pet.project.app.dto.user

import pet.project.app.exception.MappingNullValueException
import pet.project.app.model.User

object UserMapper {

    fun RequestUserDto.toModel() = User(
        login,
        contact,
        bookWishList
    )

    fun User.toDto() = ResponseUserDto(
        id,
        login ?: throw MappingNullValueException("login", "User"),
        contact ?: throw MappingNullValueException("contact", "User"),
        bookWishList ?:throw MappingNullValueException("bookWishList", "User"),
    )

}
