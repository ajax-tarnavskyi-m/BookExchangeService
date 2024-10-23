package pet.project.gateway.mapper

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import pet.project.gateway.dto.user.CreateUserExternalRequest
import pet.project.gateway.dto.user.UpdateUserExternalRequest
import pet.project.gateway.mapper.UserRequestMapper.toAddBookToUsersWishListRequest
import pet.project.gateway.mapper.UserRequestMapper.toDeleteUserByIdRequest
import pet.project.gateway.mapper.UserRequestMapper.toFindUserByIdRequest
import pet.project.gateway.mapper.UserRequestMapper.toProto
import pet.project.gateway.mapper.UserRequestMapper.toUpdateUserRequest
import pet.project.internal.commonmodels.user.User
import pet.project.internal.input.reqreply.user.AddBookToUsersWishListRequest
import pet.project.internal.input.reqreply.user.CreateUserRequest
import pet.project.internal.input.reqreply.user.UpdateUserRequest
import pet.project.internal.input.reqreply.user.UpdateUserRequest.WishListUpdate
import kotlin.test.Test

class UserRequestMapperTest {
    private val exampleUser = User.newBuilder()
        .setLogin("ClassicReader")
        .setEmail("classics.fan@example.com")
        .addAllBookWishList(setOf(ObjectId.get().toHexString()))
        .build()

    @Test
    fun `should map CreateUserExternalRequest to CreateUserRequest proto`() {
        // GIVEN
        val request = CreateUserExternalRequest(
            exampleUser.login,
            exampleUser.email,
            exampleUser.bookWishListList.toSet()
        )
        val expected = CreateUserRequest.newBuilder()
            .setLogin(exampleUser.login)
            .setEmail(exampleUser.email)
            .addAllBookWishList(exampleUser.bookWishListList)
            .build()

        // WHEN
        val actual = request.toProto()

        // THEN
        assertEquals(expected, actual)
    }

    @Test
    fun `should create FindUserByIdRequest from userId`() {
        // GIVEN
        val userId = ObjectId.get().toHexString()

        // WHEN
        val request = toFindUserByIdRequest(userId)

        // THEN
        assertEquals(userId, request.id)
    }

    @Test
    fun `should create AddBookToUsersWishListRequest from userId and bookId`() {
        // GIVEN
        val userId = ObjectId.get().toHexString()
        val bookId = ObjectId.get().toHexString()
        val expected = AddBookToUsersWishListRequest.newBuilder().setUserId(userId).setBookId(bookId).build()

        // WHEN
        val actual = toAddBookToUsersWishListRequest(userId, bookId)

        // THEN
        assertEquals(expected, actual)
    }

    @Test
    fun `should map UpdateUserExternalRequest with bookWishList to UpdateUserRequest`() {
        // GIVEN
        val userId = ObjectId.get().toHexString()
        val bookId = ObjectId.get().toHexString()
        val externalRequest = UpdateUserExternalRequest(exampleUser.login, exampleUser.email, setOf(bookId))
        val expected = UpdateUserRequest.newBuilder()
            .setId(userId)
            .setLogin(exampleUser.login)
            .setEmail(exampleUser.email)
            .setBookWishList(WishListUpdate.newBuilder().addAllBookIds(setOf(bookId)).build())
            .build()

        // WHEN
        val actual = toUpdateUserRequest(userId, externalRequest)

        // THEN
        assertEquals(expected, actual)
    }

    @Test
    fun `should map UpdateUserExternalRequest without bookWishList to UpdateUserRequest`() {
        // GIVEN
        val userId = ObjectId.get().toHexString()
        val nullWishListRequest = UpdateUserExternalRequest(exampleUser.login, exampleUser.email, null)
        val expected = UpdateUserRequest.newBuilder()
            .setId(userId)
            .setLogin(exampleUser.login)
            .setEmail(exampleUser.email)
            .build()

        // WHEN
        val actual = toUpdateUserRequest(userId, nullWishListRequest)

        // THEN
        assertFalse(actual.hasBookWishList(), "Should not have book wish list")
        assertEquals(expected, actual)
    }

    @Test
    fun `should create DeleteUserByIdRequest from userId`() {
        // GIVEN
        val userId = ObjectId.get().toHexString()

        // WHEN
        val request = toDeleteUserByIdRequest(userId)

        // THEN
        assertEquals(userId, request.id)
    }
}
