package pet.project.gateway.mapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import pet.project.core.RandomTestFields.Book.bookIdString
import pet.project.core.RandomTestFields.User.email
import pet.project.core.RandomTestFields.User.login
import pet.project.core.exception.BookNotFoundException
import pet.project.core.exception.UserNotFoundException
import pet.project.gateway.dto.user.UserExternalResponse
import pet.project.gateway.mapper.UserResponseMapper.handleResponse
import pet.project.gateway.mapper.UserResponseMapper.toExternal
import pet.project.gateway.mapper.UserResponseMapper.toExternalResponse
import pet.project.internal.commonmodels.user.User
import pet.project.internal.input.reqreply.user.AddBookToUsersWishListResponse
import pet.project.internal.input.reqreply.user.CreateUserResponse
import pet.project.internal.input.reqreply.user.DeleteUserByIdResponse
import pet.project.internal.input.reqreply.user.FindUserByIdResponse
import pet.project.internal.input.reqreply.user.UpdateUserResponse
import kotlin.test.Test

class UserResponseMapperTest {

    private val testUser = User.newBuilder()
        .setId(bookIdString)
        .setLogin(login)
        .setEmail(email)
        .addAllBookWishList(setOf(bookIdString))
        .build()

    private val expectedExternalResponse =
        UserExternalResponse(testUser.id, testUser.login, testUser.email, testUser.bookWishListList.toSet())

    @Test
    fun `should map CreateUserResponse SUCCESS to UserExternalResponse`() {
        // GIVEN
        val successResponse = CreateUserResponse.newBuilder()
            .setSuccess(CreateUserResponse.Success.newBuilder().setUser(testUser))
            .build()

        // WHEN
        val actual = successResponse.toExternalResponse()

        // THEN
        assertEquals(expectedExternalResponse, actual)
    }

    @Test
    fun `should map CreateUserResponse FAILURE to exception`() {
        // GIVEN
        val failureMessage = "User creation failed"
        val failureResponse = CreateUserResponse.newBuilder()
            .setFailure(CreateUserResponse.Failure.newBuilder().setMessage(failureMessage))
            .build()

        // WHEN
        val exception = assertThrows<RuntimeException> {
            failureResponse.toExternalResponse()
        }

        // THEN
        assertEquals(failureMessage, exception.message)
    }

    @Test
    fun `should throw exception for RESPONSE_NOT_SET`() {
        // GIVEN
        val emptyResponse = CreateUserResponse.newBuilder().build()

        // WHEN & THEN
        val exception = assertThrows<RuntimeException> {
            emptyResponse.toExternalResponse()
        }

        assertEquals("Acquired message is empty!", exception.message)
    }

    @Test
    fun `should map FindUserByIdResponse SUCCESS to UserExternalResponse`() {
        // GIVEN
        val successResponse = FindUserByIdResponse.newBuilder()
            .setSuccess(FindUserByIdResponse.Success.newBuilder().setUser(testUser))
            .build()

        // WHEN
        val actual = successResponse.toExternalResponse()

        // THEN
        assertEquals(expectedExternalResponse, actual)
    }

    @Test
    fun `should throw UserNotFoundException for USER_NOT_FOUND failure when mapping FindUserByIdResponse`() {
        // GIVEN
        val failureMessage = "User not found"
        val failureResponse = FindUserByIdResponse.newBuilder().apply {
            failureBuilder.message = failureMessage
            failureBuilder.userNotFoundBuilder
        }.build()

        // WHEN
        val exception = assertThrows<UserNotFoundException> {
            failureResponse.toExternalResponse()
        }

        // THEN
        assertEquals(failureMessage, exception.message)
    }

    @Test
    fun `should throw RuntimeException for ERROR_NOT_SET in failure when mapping FindUserByIdResponse`() {
        // GIVEN
        val failureMessage = "Error not set"
        val failureResponse = FindUserByIdResponse.newBuilder()
            .setFailure(FindUserByIdResponse.Failure.newBuilder().setMessage(failureMessage))
            .build()

        // WHEN
        val exception = assertThrows<RuntimeException> {
            failureResponse.toExternalResponse()
        }

        // THEN
        assertEquals(failureMessage, exception.message)
    }

    @Test
    fun `should throw RuntimeException for RESPONSE_NOT_SET when mapping FindUserByIdResponse`() {
        // GIVEN
        val emptyResponse = FindUserByIdResponse.newBuilder().build()

        // WHEN & THEN
        val exception = assertThrows<RuntimeException> {
            emptyResponse.toExternalResponse()
        }
        assertEquals("Acquired message is empty!", exception.message)
    }

    @Test
    fun `should map UpdateUserResponse SUCCESS to UserExternalResponse`() {
        // GIVEN
        val successResponse = UpdateUserResponse.newBuilder()
            .setSuccess(UpdateUserResponse.Success.newBuilder().setUser(testUser))
            .build()

        // WHEN
        val actual = successResponse.toExternalResponse()

        // THEN
        assertEquals(expectedExternalResponse, actual)
    }

    @Test
    fun `should throw UserNotFoundException for USER_NOT_FOUND failure when mapping UpdateUserResponse`() {
        // GIVEN
        val failureMessage = "User not found"
        val failureResponse = UpdateUserResponse.newBuilder().apply {
            failureBuilder.message = failureMessage
            failureBuilder.userNotFoundBuilder
        }.build()

        // WHEN
        val exception = assertThrows<UserNotFoundException> {
            failureResponse.toExternalResponse()
        }

        // THEN
        assertEquals(failureMessage, exception.message)
    }

    @Test
    fun `should throw RuntimeException for ERROR_NOT_SET in failure`() {
        // GIVEN
        val failureMessage = "Error not set"
        val failureResponse = UpdateUserResponse.newBuilder()
            .setFailure(UpdateUserResponse.Failure.newBuilder().setMessage(failureMessage))
            .build()

        // WHEN
        val exception = assertThrows<RuntimeException> {
            failureResponse.toExternalResponse()
        }

        // THEN
        assertEquals(failureMessage, exception.message)
    }

    @Test
    fun `should throw RuntimeException for RESPONSE_NOT_SET`() {
        // GIVEN
        val emptyResponse = UpdateUserResponse.newBuilder().build()

        // WHEN & THEN
        val exception = assertThrows<RuntimeException> {
            emptyResponse.toExternalResponse()
        }
        assertEquals("Acquired message is empty!", exception.message)
    }

    @Test
    fun `should return Unit on SUCCESS`() {
        // GIVEN
        val successResponse = AddBookToUsersWishListResponse.newBuilder()
            .setSuccess(AddBookToUsersWishListResponse.Success.newBuilder().build())
            .build()

        // WHEN
        val result = successResponse.toExternal()

        // THEN
        assertEquals(Unit, result)
    }

    @Test
    fun `should throw UserNotFoundException for USER_NOT_FOUND failure`() {
        // GIVEN
        val failureMessage = "User not found"
        val failureResponse = AddBookToUsersWishListResponse.newBuilder().apply {
            failureBuilder.message = failureMessage
            failureBuilder.userNotFoundBuilder
        }.build()

        // WHEN
        val exception = assertThrows<UserNotFoundException> {
            failureResponse.toExternal()
        }

        // THEN
        assertEquals(failureMessage, exception.message)
    }

    @Test
    fun `should throw BookNotFoundException for BOOK_NOT_FOUND failure`() {
        // GIVEN
        val failureMessage = "Book not found"
        val failureResponse = AddBookToUsersWishListResponse.newBuilder().apply {
            failureBuilder.message = failureMessage
            failureBuilder.bookNotFoundBuilder
        }.build()

        // WHEN
        val exception = assertThrows<BookNotFoundException> {
            failureResponse.toExternal()
        }

        // THEN
        assertEquals(failureMessage, exception.message)
    }

    @Test
    fun `should throw RuntimeException for ERROR_NOT_SET in failure when mapping AddBookToUsersWishListResponse`() {
        // GIVEN
        val failureMessage = "Error not set"
        val failureResponse = AddBookToUsersWishListResponse.newBuilder()
            .setFailure(AddBookToUsersWishListResponse.Failure.newBuilder().setMessage(failureMessage))
            .build()

        // WHEN
        val exception = assertThrows<RuntimeException> {
            failureResponse.toExternal()
        }

        // THEN
        assertEquals(failureMessage, exception.message)
    }

    @Test
    fun `should throw RuntimeException for RESPONSE_NOT_SET when mapping AddBookToUsersWishListResponse`() {
        // GIVEN
        val emptyResponse = AddBookToUsersWishListResponse.newBuilder().build()

        // WHEN & THEN
        val exception = assertThrows<RuntimeException> {
            emptyResponse.toExternal()
        }

        assertEquals("Acquired message is empty!", exception.message)
    }

    @Test
    fun `should return Unit on SUCCESS when mapping DeleteUserByIdResponse`() {
        // GIVEN
        val successResponse = DeleteUserByIdResponse.newBuilder()
            .setSuccess(DeleteUserByIdResponse.Success.newBuilder().build())
            .build()

        // WHEN
        val result = successResponse.handleResponse()

        // THEN
        assertEquals(Unit, result)
    }

    @Test
    fun `should throw RuntimeException for failure when mapping DeleteUserByIdResponse`() {
        // GIVEN
        val failureMessage = "Deletion failed"
        val failureResponse = DeleteUserByIdResponse.newBuilder()
            .setFailure(DeleteUserByIdResponse.Failure.newBuilder().setMessage(failureMessage))
            .build()

        // WHEN
        val exception = assertThrows<RuntimeException> {
            failureResponse.handleResponse()
        }

        // THEN
        assertEquals(failureMessage, exception.message)
    }
}
