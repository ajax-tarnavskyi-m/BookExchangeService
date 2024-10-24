package pet.project.gateway.mapper

import pet.project.core.exception.BookNotFoundException
import pet.project.core.exception.UserNotFoundException
import pet.project.gateway.dto.user.UserExternalResponse
import pet.project.internal.commonmodels.user.User
import pet.project.internal.input.reqreply.user.AddBookToUsersWishListResponse
import pet.project.internal.input.reqreply.user.CreateUserResponse
import pet.project.internal.input.reqreply.user.DeleteUserByIdResponse
import pet.project.internal.input.reqreply.user.FindUserByIdResponse
import pet.project.internal.input.reqreply.user.UpdateUserResponse

object UserResponseMapper {

    fun CreateUserResponse.toExternalResponse(): UserExternalResponse {
        return when (this.responseCase!!) {
            CreateUserResponse.ResponseCase.SUCCESS -> success.user.toExternalResponse()
            CreateUserResponse.ResponseCase.FAILURE -> error(failure.message)
            CreateUserResponse.ResponseCase.RESPONSE_NOT_SET -> error("Acquired message is empty!")
        }
    }

    fun FindUserByIdResponse.toExternalResponse(): UserExternalResponse {
        return when (this.responseCase!!) {
            FindUserByIdResponse.ResponseCase.SUCCESS -> success.user.toExternalResponse()
            FindUserByIdResponse.ResponseCase.FAILURE -> failure.asException()
            FindUserByIdResponse.ResponseCase.RESPONSE_NOT_SET -> error("Acquired message is empty!")
        }
    }

    private fun FindUserByIdResponse.Failure.asException(): Nothing {
        throw when (errorCase!!) {
            FindUserByIdResponse.Failure.ErrorCase.USER_NOT_FOUND -> UserNotFoundException(message)
            FindUserByIdResponse.Failure.ErrorCase.ERROR_NOT_SET -> error(message)
        }
    }

    fun UpdateUserResponse.toExternalResponse(): UserExternalResponse {
        return when (this.responseCase!!) {
            UpdateUserResponse.ResponseCase.SUCCESS -> success.user.toExternalResponse()
            UpdateUserResponse.ResponseCase.FAILURE -> failure.asException()
            UpdateUserResponse.ResponseCase.RESPONSE_NOT_SET -> error("Acquired message is empty!")
        }
    }

    private fun UpdateUserResponse.Failure.asException(): Nothing {
        throw when (errorCase!!) {
            UpdateUserResponse.Failure.ErrorCase.USER_NOT_FOUND -> UserNotFoundException(message)
            UpdateUserResponse.Failure.ErrorCase.ERROR_NOT_SET -> error(message)
        }
    }

    fun AddBookToUsersWishListResponse.toExternal() {
        return when (this.responseCase!!) {
            AddBookToUsersWishListResponse.ResponseCase.SUCCESS -> Unit
            AddBookToUsersWishListResponse.ResponseCase.FAILURE -> failure.asException()
            AddBookToUsersWishListResponse.ResponseCase.RESPONSE_NOT_SET -> error("Acquired message is empty!")
        }
    }

    private fun AddBookToUsersWishListResponse.Failure.asException() : Nothing {
        throw when (errorCase!!) {
            AddBookToUsersWishListResponse.Failure.ErrorCase.USER_NOT_FOUND -> UserNotFoundException(message)
            AddBookToUsersWishListResponse.Failure.ErrorCase.BOOK_NOT_FOUND -> BookNotFoundException(message)
            AddBookToUsersWishListResponse.Failure.ErrorCase.ERROR_NOT_SET -> error(message)
        }
    }

    fun DeleteUserByIdResponse.handleResponse() {
        if (hasFailure()) {
            error(failure.message)
        }
    }

    private fun User.toExternalResponse(): UserExternalResponse {
        return UserExternalResponse(id, login, email, bookWishListList.toSet())
    }
}
