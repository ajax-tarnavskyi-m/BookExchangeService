package pet.project.gateway.mapper

import pet.project.gateway.dto.user.CreateUserExternalRequest
import pet.project.gateway.dto.user.UpdateUserExternalRequest
import pet.project.internal.input.reqreply.user.AddBookToUsersWishListRequest
import pet.project.internal.input.reqreply.user.CreateUserRequest
import pet.project.internal.input.reqreply.user.DeleteUserByIdRequest
import pet.project.internal.input.reqreply.user.FindUserByIdRequest
import pet.project.internal.input.reqreply.user.UpdateUserRequest
import pet.project.internal.input.reqreply.user.UpdateUserRequest.WishListUpdate

object UserRequestMapper {

    fun CreateUserExternalRequest.toProto(): CreateUserRequest {
        return CreateUserRequest.newBuilder().also {
            it.login = login
            it.email = email
            it.addAllBookWishList(bookWishList)
        }.build()
    }

    fun toFindUserByIdRequest(userId: String): FindUserByIdRequest {
        return FindUserByIdRequest.newBuilder()
            .setId(userId)
            .build()
    }

    fun toAddBookToUsersWishListRequest(userId: String, bookId: String): AddBookToUsersWishListRequest {
        return AddBookToUsersWishListRequest.newBuilder().also {
            it.userId = userId
            it.bookId = bookId
        }.build()
    }

    fun toUpdateUserRequest(userId: String, request: UpdateUserExternalRequest): UpdateUserRequest {
        return UpdateUserRequest.newBuilder().apply {
            id = userId
            request.login?.let { newLogin -> login = newLogin }
            request.email?.let { newEmail -> email = newEmail }
            request.bookWishList?.let { newWishList ->
                bookWishList = WishListUpdate.newBuilder().addAllBookIds(newWishList).build()
            }
        }.build()
    }

    fun toDeleteUserByIdRequest(userId: String): DeleteUserByIdRequest {
        return DeleteUserByIdRequest.newBuilder().setId(userId).build()
    }
}
