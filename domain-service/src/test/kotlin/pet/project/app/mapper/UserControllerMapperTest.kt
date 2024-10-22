package pet.project.app.mapper

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import pet.project.app.mapper.UserControllerMapper.toAddBookToUserWishListResponse
import pet.project.app.mapper.UserControllerMapper.toCreateUserResponse
import pet.project.app.mapper.UserControllerMapper.toDeleteUserByIdResponse
import pet.project.app.mapper.UserControllerMapper.toFindUserByIdResponse
import pet.project.app.mapper.UserControllerMapper.toProto
import pet.project.app.mapper.UserControllerMapper.toUpdateUserResponse
import pet.project.app.model.domain.DomainUser
import kotlin.test.Test
import kotlin.test.assertTrue

class UserControllerMapperTest {

    private val exampleDomainUser = DomainUser(
        ObjectId.get().toHexString(),
        "testLogin",
        "test@example.com",
        setOf(ObjectId.get().toHexString())
    )

    @Test
    fun `should map DomainUser to CreateUserResponse with success`() {
        // WHEN
        val createUserResponse = exampleDomainUser.toCreateUserResponse()

        // THEN
        val protoUser = createUserResponse.success.user
        assertEquals(exampleDomainUser.id, protoUser.id)
        assertEquals(exampleDomainUser.login, protoUser.login)
        assertEquals(exampleDomainUser.email, protoUser.email)
        assertEquals(exampleDomainUser.bookWishList.toList(), protoUser.bookWishListList)
    }

    @Test
    fun `should map DomainUser to Proto User`() {
        // WHEN
        val protoUser = exampleDomainUser.toProto()

        // THEN
        assertEquals(exampleDomainUser.id, protoUser.id)
        assertEquals(exampleDomainUser.login, protoUser.login)
        assertEquals(exampleDomainUser.email, protoUser.email)
        assertEquals(exampleDomainUser.bookWishList.toList(), protoUser.bookWishListList)
    }

    @Test
    fun `should create AddBookToUsersWishListResponse with success`() {
        // WHEN
        val response = toAddBookToUserWishListResponse()

        // THEN
        assertTrue(response.hasSuccess(), "Response should contain success")
    }

    @Test
    fun `should map DomainUser to FindUserByIdResponse with success`() {
        // WHEN
        val response = exampleDomainUser.toFindUserByIdResponse()

        // THEN
        assertTrue(response.hasSuccess(), "Response should contain success")
        val protoUser = response.success.user
        assertEquals(exampleDomainUser.id, protoUser.id)
        assertEquals(exampleDomainUser.login, protoUser.login)
        assertEquals(exampleDomainUser.email, protoUser.email)
        assertEquals(exampleDomainUser.bookWishList.toList(), protoUser.bookWishListList)
    }

    @Test
    fun `should map DomainUser to UpdateUserResponse with success`() {
        // WHEN
        val response = exampleDomainUser.toUpdateUserResponse()

        // THEN
        assertTrue(response.hasSuccess(), "Response should contain success")
        val protoUser = response.success.user
        assertEquals(exampleDomainUser.id, protoUser.id)
        assertEquals(exampleDomainUser.login, protoUser.login)
        assertEquals(exampleDomainUser.email, protoUser.email)
        assertEquals(exampleDomainUser.bookWishList.toList(), protoUser.bookWishListList)
    }

    @Test
    fun `should create DeleteUserByIdResponse with success`() {
        // WHEN
        val response = toDeleteUserByIdResponse()

        // THEN
        assertTrue(response.hasSuccess(), "Response should contain success")
    }
}
