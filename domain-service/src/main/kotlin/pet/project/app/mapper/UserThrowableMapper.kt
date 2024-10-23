package pet.project.app.mapper

import pet.project.core.exception.BookNotFoundException
import pet.project.core.exception.UserNotFoundException
import pet.project.internal.input.reqreply.user.AddBookToUsersWishListResponse
import pet.project.internal.input.reqreply.user.CreateUserResponse
import pet.project.internal.input.reqreply.user.DeleteUserByIdResponse
import pet.project.internal.input.reqreply.user.FindUserByIdResponse
import pet.project.internal.input.reqreply.user.UpdateUserResponse

object UserThrowableMapper {

    fun Throwable.toFailureCreateUserResponse(): CreateUserResponse {
        return CreateUserResponse.newBuilder().apply {
            failureBuilder.message = message.orEmpty()
        }.build()
    }

    fun Throwable.toFailureAddBookToUserWishListResponse(): AddBookToUsersWishListResponse {
        return AddBookToUsersWishListResponse.newBuilder().apply {
            failureBuilder.message = message.orEmpty()
            when (this@toFailureAddBookToUserWishListResponse) {
                is UserNotFoundException -> failureBuilder.userNotFoundBuilder
                is BookNotFoundException -> failureBuilder.bookNotFoundBuilder
            }
        }.build()
    }

    fun Throwable.toFailureFindUserByIdResponse(): FindUserByIdResponse {
        return FindUserByIdResponse.newBuilder().apply {
            failureBuilder.message = message.orEmpty()
            when (this@toFailureFindUserByIdResponse) {
                is UserNotFoundException -> failureBuilder.userNotFoundBuilder
            }
        }.build()
    }

    fun Throwable.toFailureUpdateUserResponse(): UpdateUserResponse {
        return UpdateUserResponse.newBuilder().apply {
            failureBuilder.message = message.orEmpty()
            when (this@toFailureUpdateUserResponse) {
                is UserNotFoundException -> failureBuilder.userNotFoundBuilder
            }
        }.build()
    }

    fun Throwable.toFailureDeleteUserByIdResponse(): DeleteUserByIdResponse {
        return DeleteUserByIdResponse.newBuilder().apply {
            failureBuilder.message = message.orEmpty()
        }.build()
    }
}
