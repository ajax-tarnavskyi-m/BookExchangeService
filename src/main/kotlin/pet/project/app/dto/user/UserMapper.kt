package pet.project.app.dto.user

import pet.project.app.model.User

object UserMapper {

    fun RequestUserDto.toModel(): User {
        return User(
            this.login,
            this.contact,
            this.bookWishList
        )
    }

    fun User.toDto(): ResponseUserDto {
        return ResponseUserDto(
            this.id,
            this.login,
            this.contact,
            this.bookWishList
        )
    }
}
