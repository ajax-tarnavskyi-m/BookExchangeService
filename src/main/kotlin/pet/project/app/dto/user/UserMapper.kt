package pet.project.app.dto.user

import pet.project.app.model.User

object UserMapper {

    fun RequestSaveUserDto.toModel() = User(
        login = login,
        bookWishList = bookWishList
    )

    fun RequestUpdateUserDto.toModel() = User(
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
