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
            it.setLogin(login)
            it.setEmail(email)
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
            it.setUserId(userId)
            it.setBookId(bookId)
        }.build()
    }

    fun toUpdateUserRequest(userId: String, request: UpdateUserExternalRequest): UpdateUserRequest {
        return UpdateUserRequest.newBuilder().apply {
            setId(userId)
            setLogin(request.login)
            setEmail(request.email)
            request.bookWishList?.let {
                setBookWishList(WishListUpdate.newBuilder().addAllBookIds(it))
            }
        }.build()
    }

    fun toDeleteUserByIdRequest(userId: String): DeleteUserByIdRequest {
        return DeleteUserByIdRequest.newBuilder().setId(userId).build()
    }
}
