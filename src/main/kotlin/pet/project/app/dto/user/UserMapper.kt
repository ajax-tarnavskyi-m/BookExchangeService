package pet.project.app.dto.user

import pet.project.app.exception.MappingNullValueException
import pet.project.app.model.User

object UserMapper {

    fun RequestUserDto.toModel() = User(
        login,
        bookWishList
    )

    fun User.toDto() = ResponseUserDto(
        id,
        login ?: "",
        bookWishList ?: emptySet(),
    )

}
