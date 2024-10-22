package pet.project.gateway.mapper

import pet.project.core.exception.BookNotFoundException
import pet.project.core.exception.UserNotFoundException
import pet.project.gateway.dto.user.UserExternalResponse
import pet.project.internal.commonmodels.user.user.User
import pet.project.internal.input.reqreply.user.add_book_to_wish_list.AddBookToUsersWishListResponse
import pet.project.internal.input.reqreply.user.create.CreateUserResponse
import pet.project.internal.input.reqreply.user.delete.DeleteUserByIdResponse
import pet.project.internal.input.reqreply.user.find.FindUserByIdResponse
import pet.project.internal.input.reqreply.user.update.UpdateUserResponse

object UserResponseProtoMapper {

    fun CreateUserResponse.toExternalResponse(): UserExternalResponse {
        return when (this.responseCase!!) {
            CreateUserResponse.ResponseCase.SUCCESS -> success.user.toExternalResponse()
            CreateUserResponse.ResponseCase.FAILURE -> error(failure.message)
            CreateUserResponse.ResponseCase.RESPONSE_NOT_SET -> throw RuntimeException("Acquired message is empty!")
        }
    }

    fun FindUserByIdResponse.toExternalResponse(): UserExternalResponse {
        return when (this.responseCase!!) {
            FindUserByIdResponse.ResponseCase.SUCCESS -> success.user.toExternalResponse()
            FindUserByIdResponse.ResponseCase.FAILURE -> {
                when (failure.errorCase!!) {
                    FindUserByIdResponse.Failure.ErrorCase.USER_NOT_FOUND -> throw UserNotFoundException(failure.message)
                    FindUserByIdResponse.Failure.ErrorCase.ERROR_NOT_SET -> error(failure.message)
                }
            }

            FindUserByIdResponse.ResponseCase.RESPONSE_NOT_SET -> throw RuntimeException("Acquired message is empty!")
        }
    }

    fun UpdateUserResponse.toExternalResponse(): UserExternalResponse {
        return when (this.responseCase!!) {
            UpdateUserResponse.ResponseCase.SUCCESS -> success.user.toExternalResponse()
            UpdateUserResponse.ResponseCase.FAILURE -> {
                when (failure.errorCase!!) {
                    UpdateUserResponse.Failure.ErrorCase.USER_NOT_FOUND,
                        -> throw UserNotFoundException(failure.message)

                    UpdateUserResponse.Failure.ErrorCase.ERROR_NOT_SET -> error(failure.message)
                }
            }

            UpdateUserResponse.ResponseCase.RESPONSE_NOT_SET -> throw RuntimeException("Acquired message is empty!")
        }
    }

    fun AddBookToUsersWishListResponse.toExternal() {
        return when (this.responseCase!!) {
            AddBookToUsersWishListResponse.ResponseCase.SUCCESS -> Unit
            AddBookToUsersWishListResponse.ResponseCase.FAILURE -> {
                when (failure.errorCase!!) {
                    AddBookToUsersWishListResponse.Failure.ErrorCase.USER_NOT_FOUND, -> throw UserNotFoundException(failure.message)
                    AddBookToUsersWishListResponse.Failure.ErrorCase.BOOK_NOT_FOUND, -> throw BookNotFoundException(failure.message)
                    AddBookToUsersWishListResponse.Failure.ErrorCase.ERROR_NOT_SET -> error(failure.message)
                }
            }

            AddBookToUsersWishListResponse.ResponseCase.RESPONSE_NOT_SET -> throw RuntimeException("Acquired message is empty!")
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
