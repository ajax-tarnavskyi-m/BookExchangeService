package pet.project.app.mapper

import pet.project.app.model.domain.DomainUser
import pet.project.internal.commonmodels.user.user.User
import pet.project.internal.input.reqreply.user.add_book_to_wish_list.AddBookToUsersWishListResponse
import pet.project.internal.input.reqreply.user.create.CreateUserResponse
import pet.project.internal.input.reqreply.user.delete.DeleteUserByIdResponse
import pet.project.internal.input.reqreply.user.find.FindUserByIdResponse
import pet.project.internal.input.reqreply.user.update.UpdateUserResponse

object UserControllerMapper {

    fun DomainUser.toCreateUserResponse(): CreateUserResponse {
        return CreateUserResponse.newBuilder().also {
            it.successBuilder.user = this.toProto()
        }.build()
    }

    fun DomainUser.toProto(): User {
        return User.newBuilder().also {
            it.setId(id)
            it.setEmail(email)
            it.setLogin(login)
            it.addAllBookWishList(bookWishList)
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

    fun toDeleteUserByIdResponse() : DeleteUserByIdResponse {
        return DeleteUserByIdResponse.newBuilder().apply { successBuilder }.build()
    }
}
