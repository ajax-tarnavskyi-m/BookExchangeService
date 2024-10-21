package pet.project.gateway.mapper

import pet.project.gateway.dto.user.UserExternalResponse
import pet.project.internal.commonmodels.user.user.User
import pet.project.internal.input.reqreply.user.add_book_to_wish_list.AddBookToUsersWishListResponse
import pet.project.internal.input.reqreply.user.create.CreateUserResponse
import pet.project.internal.input.reqreply.user.delete.DeleteUserByIdResponse
import pet.project.internal.input.reqreply.user.find.FindUserByIdResponse
import pet.project.internal.input.reqreply.user.update.UpdateUserResponse

object UserResponseProtoMapper {

    fun CreateUserResponse.toExternal(): UserExternalResponse {
        return success.user.toExternal()
    }

    fun FindUserByIdResponse.toExternal(): UserExternalResponse {
        return this.success.user.toExternal()
    }

    fun UpdateUserResponse.toExternal(): UserExternalResponse {
        return success.user.toExternal()
    }

    fun AddBookToUsersWishListResponse.toUnit() {
        if (hasFailure()) {
            error(failure.message)
        }
    }

    fun DeleteUserByIdResponse.toUnit() {
        if (hasFailure()) {
            error(failure.message)
        }
    }

    private fun User.toExternal(): UserExternalResponse {
        return UserExternalResponse(id, login, email, bookWishListList.toSet())
    }
}
