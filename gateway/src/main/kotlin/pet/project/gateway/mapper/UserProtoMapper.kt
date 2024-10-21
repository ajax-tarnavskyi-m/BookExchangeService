package pet.project.gateway.mapper

import pet.project.gateway.dto.user.CreateUserExternalRequest
import pet.project.gateway.dto.user.UpdateUserExternalRequest
import pet.project.gateway.dto.user.UserExternalResponse
import pet.project.internal.commonmodels.user.user.User
import pet.project.internal.input.reqreply.user.add_book_to_wish_list.AddBookToUsersWishListRequest
import pet.project.internal.input.reqreply.user.add_book_to_wish_list.AddBookToUsersWishListResponse
import pet.project.internal.input.reqreply.user.create.CreateUserRequest
import pet.project.internal.input.reqreply.user.create.CreateUserResponse
import pet.project.internal.input.reqreply.user.delete.DeleteUserByIdRequest
import pet.project.internal.input.reqreply.user.delete.DeleteUserByIdResponse
import pet.project.internal.input.reqreply.user.find.FindUserByIdRequest
import pet.project.internal.input.reqreply.user.find.FindUserByIdResponse
import pet.project.internal.input.reqreply.user.update.UpdateUserRequest
import pet.project.internal.input.reqreply.user.update.UpdateUserRequest.WishListUpdate
import pet.project.internal.input.reqreply.user.update.UpdateUserResponse

object UserProtoMapper {

    fun CreateUserExternalRequest.toProto(): CreateUserRequest {
        val user = User.newBuilder().also {
            it.setLogin(login)
            it.setEmail(email)
            it.addAllBookWishList(bookWishList)
        }.build()

        return CreateUserRequest.newBuilder()
            .setUser(user)
            .build()
    }

    fun CreateUserResponse.toExternal(): UserExternalResponse {
        return success.user.toExternal()
    }

    private fun User.toExternal(): UserExternalResponse {
        return UserExternalResponse(id, login, email, bookWishListList.toSet())
    }

    fun toFindUserByIdRequest(userId: String): FindUserByIdRequest {
        return FindUserByIdRequest.newBuilder()
            .setId(userId)
            .build()
    }

    fun FindUserByIdResponse.toExternal(): UserExternalResponse {
        return this.success.user.toExternal()
    }

    fun toAddBookToUsersWishListRequest(userId: String, bookId: String): AddBookToUsersWishListRequest {
        return AddBookToUsersWishListRequest.newBuilder().also {
            it.setUserId(userId)
            it.setBookId(bookId)
        }.build()
    }

    fun AddBookToUsersWishListResponse.toUnit() {
        if (hasFailure()) {
            error(failure.message)
        }
    }

    fun toUpdateUserRequest(userId: String, request: UpdateUserExternalRequest): UpdateUserRequest {
        return UpdateUserRequest.newBuilder().apply {
            setId(userId)
            setLogin(request.login)
            setEmail(request.email)
            request.bookWishList?.let<Set<String>, Unit> {
                setBookWishList(WishListUpdate.newBuilder().addAllBookIds(it))
            }
        }.build()
    }

    fun UpdateUserResponse.toExternal(): UserExternalResponse {
        return success.user.toExternal()
    }

    fun toDeleteUserByIdRequest(userId: String) : DeleteUserByIdRequest {
        return DeleteUserByIdRequest.newBuilder().setId(userId).build()
    }

    fun DeleteUserByIdResponse.toUnit() {
        if (hasFailure()) {
            error(failure.message)
        }
    }
}
