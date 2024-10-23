package pet.project.app.mapper

import pet.project.app.model.domain.DomainUser
import pet.project.internal.commonmodels.user.User
import pet.project.internal.input.reqreply.user.AddBookToUsersWishListResponse
import pet.project.internal.input.reqreply.user.CreateUserResponse
import pet.project.internal.input.reqreply.user.DeleteUserByIdResponse
import pet.project.internal.input.reqreply.user.FindUserByIdResponse
import pet.project.internal.input.reqreply.user.UpdateUserResponse

object UserResponseMapper {

    fun DomainUser.toCreateUserResponse(): CreateUserResponse {
        return CreateUserResponse.newBuilder().also {
            it.successBuilder.user = this.toProto()
        }.build()
    }

    fun toAddBookToUserWishListResponse(): AddBookToUsersWishListResponse {
        return AddBookToUsersWishListResponse.newBuilder()
            .also { it.successBuilder }
            .build()
    }

    fun DomainUser.toFindUserByIdResponse(): FindUserByIdResponse {
        return FindUserByIdResponse.newBuilder().also {
            it.successBuilder.user = this.toProto()
        }.build()
    }

    fun DomainUser.toUpdateUserResponse(): UpdateUserResponse {
        return UpdateUserResponse.newBuilder().also {
            it.successBuilder.user = toProto()
        }.build()
    }

    fun toDeleteUserByIdResponse(): DeleteUserByIdResponse {
        return DeleteUserByIdResponse.newBuilder().apply { successBuilder }.build()
    }

    private fun DomainUser.toProto(): User {
        return User.newBuilder().also {
            it.id = id
            it.email = email
            it.login = login
            it.addAllBookWishList(bookWishList)
        }.build()
    }
}