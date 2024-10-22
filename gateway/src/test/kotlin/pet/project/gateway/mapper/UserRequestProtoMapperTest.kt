package pet.project.gateway.mapper

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import pet.project.gateway.dto.user.CreateUserExternalRequest
import pet.project.gateway.dto.user.UpdateUserExternalRequest
import pet.project.gateway.mapper.UserRequestProtoMapper.toAddBookToUsersWishListRequest
import pet.project.gateway.mapper.UserRequestProtoMapper.toDeleteUserByIdRequest
import pet.project.gateway.mapper.UserRequestProtoMapper.toFindUserByIdRequest
import pet.project.gateway.mapper.UserRequestProtoMapper.toProto
import pet.project.gateway.mapper.UserRequestProtoMapper.toUpdateUserRequest
import pet.project.internal.commonmodels.user.user.User
import kotlin.test.Test

class UserRequestProtoMapperTest {
    @Test
    fun `should map CreateUserExternalRequest to CreateUserRequest proto`() {
        // GIVEN
        val bookWishList = setOf(ObjectId.get().toHexString())
        val request = CreateUserExternalRequest("ClassicReader", "classics.fan@example.com", bookWishList)

        // WHEN
        val createUserProtoRequest = request.toProto()

        // THEN
        val userProto = createUserProtoRequest.user

        assertEquals("ClassicReader", userProto.login, "Login should be correctly mapped")
        assertEquals("classics.fan@example.com", userProto.email, "Email should be correctly mapped")
        assertEquals(bookWishList.toList(), userProto.bookWishListList, "Book wish list should be correctly mapped")
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

        // WHEN
        val request = toAddBookToUsersWishListRequest(userId, bookId)

        // THEN
        assertEquals(userId, request.userId)
        assertEquals(bookId, request.bookId)
    }

    @Test
    fun `should map UpdateUserExternalRequest with bookWishList to UpdateUserRequest`() {
        // GIVEN
        val userId = ObjectId.get().toHexString()
        val bookId = ObjectId.get().toHexString()
        val externalRequest = UpdateUserExternalRequest("newLogin", "newEmail@example.com", setOf(bookId))

        // WHEN
        val request = toUpdateUserRequest(userId, externalRequest)

        // THEN
        assertEquals(userId, request.id)
        assertEquals("newLogin", request.login)
        assertEquals("newEmail@example.com", request.email)
        assertNotNull(request.bookWishList)
        assertEquals(listOf(bookId), request.bookWishList.bookIdsList)
    }

    @Test
    fun `should map UpdateUserExternalRequest without bookWishList to UpdateUserRequest`() {
        // GIVEN
        val userId = ObjectId.get().toHexString()
        val externalRequest = UpdateUserExternalRequest("newLogin", "newEmail@example.com", null)

        // WHEN
        val request = toUpdateUserRequest(userId, externalRequest)

        // THEN
        assertEquals(userId, request.id)
        assertEquals("newLogin", request.login)
        assertEquals("newEmail@example.com", request.email)
        assertFalse(request.hasBookWishList(), "Should not have book wish list")
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

    @Test
    fun `toBulder clone`() {
        val user1 = User.newBuilder().setEmail("email").setLogin("login").build()
        val user2 = user1.toBuilder().setEmail("otherEmail").build()
        kotlin.test.assertNotEquals(user2.email, user1.email)
    }
}
