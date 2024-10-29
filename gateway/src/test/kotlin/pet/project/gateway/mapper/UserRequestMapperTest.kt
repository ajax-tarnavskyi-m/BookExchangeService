package pet.project.gateway.mapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import pet.project.core.RandomTestFields.Book.randomBookIdString
import pet.project.core.RandomTestFields.User.randomEmail
import pet.project.core.RandomTestFields.User.randomLogin
import pet.project.core.RandomTestFields.User.randomUserIdString
import pet.project.gateway.dto.user.CreateUserExternalRequest
import pet.project.gateway.dto.user.UpdateUserExternalRequest
import pet.project.gateway.mapper.UserRequestMapper.toAddBookToUsersWishListRequest
import pet.project.gateway.mapper.UserRequestMapper.toDeleteUserByIdRequest
import pet.project.gateway.mapper.UserRequestMapper.toFindUserByIdRequest
import pet.project.gateway.mapper.UserRequestMapper.toProto
import pet.project.gateway.mapper.UserRequestMapper.toUpdateUserRequest
import pet.project.internal.input.reqreply.user.AddBookToUsersWishListRequest
import pet.project.internal.input.reqreply.user.CreateUserRequest
import pet.project.internal.input.reqreply.user.FindUserByIdRequest
import pet.project.internal.input.reqreply.user.UpdateUserRequest
import pet.project.internal.input.reqreply.user.UpdateUserRequest.WishListUpdate
import kotlin.test.Test

class UserRequestMapperTest {

    @Test
    fun `should map CreateUserExternalRequest to CreateUserRequest proto`() {
        // GIVEN
        val request = CreateUserExternalRequest(randomLogin(), randomEmail(), setOf(randomBookIdString()))
        val expected = CreateUserRequest.newBuilder()
            .setLogin(request.login)
            .setEmail(request.email)
            .addAllBookWishList(request.bookWishList)
            .build()

        // WHEN
        val actual = request.toProto()

        // THEN
        assertEquals(expected, actual)
    }

    @Test
    fun `should create FindUserByIdRequest from userId`() {
        // GIVEN
        val userIdString = randomUserIdString()
        val expected = FindUserByIdRequest.newBuilder().setId(userIdString).build()

        // WHEN
        val actual = toFindUserByIdRequest(userIdString)

        // THEN
        assertEquals(expected, actual)
    }

    @Test
    fun `should create AddBookToUsersWishListRequest from userId and bookId`() {
        // GIVEN
        val userIdString = randomUserIdString()
        val bookIdString = randomBookIdString()
        val expected = AddBookToUsersWishListRequest.newBuilder()
            .setUserId(userIdString)
            .setBookId(bookIdString)
            .build()

        // WHEN
        val actual = toAddBookToUsersWishListRequest(userIdString, bookIdString)

        // THEN
        assertEquals(expected, actual)
    }

    @Test
    fun `should map UpdateUserExternalRequest with bookWishList to UpdateUserRequest`() {
        // GIVEN
        val userIdString = randomUserIdString()
        val request = UpdateUserExternalRequest(randomLogin(), randomEmail(), setOf(randomBookIdString()))
        val expected = UpdateUserRequest.newBuilder()
            .setId(userIdString)
            .setLogin(request.login)
            .setEmail(request.email)
            .setBookWishList(WishListUpdate.newBuilder().addAllBookIds(request.bookWishList).build())
            .build()

        // WHEN
        val actual = toUpdateUserRequest(userIdString, request)

        // THEN
        assertEquals(expected, actual)
    }

    @Test
    fun `should map empty UpdateUserExternalRequest to UpdateUserRequest`() {
        // GIVEN
        val userIdString = randomUserIdString()
        val externalRequest = UpdateUserExternalRequest(null, null, null)
        val expected = UpdateUserRequest.newBuilder().setId(userIdString).build()

        // WHEN
        val actual = toUpdateUserRequest(userIdString, externalRequest)

        // THEN
        assertEquals(expected, actual)
    }

    @Test
    fun `should map UpdateUserExternalRequest without bookWishList to UpdateUserRequest`() {
        // GIVEN
        val userIdString = randomUserIdString()
        val request = UpdateUserExternalRequest(randomLogin(), randomEmail(), null)
        val expected = UpdateUserRequest.newBuilder()
            .setId(userIdString)
            .setLogin(request.login)
            .setEmail(request.email)
            .build()

        // WHEN
        val actual = toUpdateUserRequest(userIdString, request)

        // THEN
        assertFalse(actual.hasBookWishList(), "Should not have book wish list")
        assertEquals(expected, actual)
    }

    @Test
    fun `should create DeleteUserByIdRequest from userId`() {
        // WHEN
        val userIdString = randomUserIdString()
        val request = toDeleteUserByIdRequest(userIdString)

        // THEN
        assertEquals(userIdString, request.id)
    }
}
