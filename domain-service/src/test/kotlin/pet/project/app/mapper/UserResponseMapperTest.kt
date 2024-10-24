package pet.project.app.mapper

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import pet.project.app.mapper.UserResponseMapper.toAddBookToUserWishListResponse
import pet.project.app.mapper.UserResponseMapper.toCreateUserResponse
import pet.project.app.mapper.UserResponseMapper.toDeleteUserByIdResponse
import pet.project.app.mapper.UserResponseMapper.toFindUserByIdResponse
import pet.project.app.mapper.UserResponseMapper.toUpdateUserResponse
import pet.project.app.model.domain.DomainUser
import pet.project.internal.commonmodels.user.User
import pet.project.internal.input.reqreply.user.CreateUserResponse
import pet.project.internal.input.reqreply.user.FindUserByIdResponse
import pet.project.internal.input.reqreply.user.UpdateUserResponse
import kotlin.test.Test
import kotlin.test.assertTrue

class UserResponseMapperTest {

    private val exampleDomainUser = DomainUser(
        ObjectId.get().toHexString(),
        "testLogin",
        "test@example.com",
        setOf(ObjectId.get().toHexString())
    )

    private val exampleProtoUser = User.newBuilder().setId(exampleDomainUser.id)
        .setEmail(exampleDomainUser.email)
        .setLogin(exampleDomainUser.login)
        .addAllBookWishList(exampleDomainUser.bookWishList)
        .build()

    @Test
    fun `should map DomainUser to CreateUserResponse with success`() {
        // GIVEN
        val expected = CreateUserResponse.newBuilder().apply { successBuilder.user = exampleProtoUser }.build()

        // WHEN
        val actual = exampleDomainUser.toCreateUserResponse()

        // THEN
        assertEquals(expected, actual)
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
        // GIVEN
        val expected = FindUserByIdResponse.newBuilder().apply { successBuilder.user = exampleProtoUser }.build()

        // WHEN
        val actual = exampleDomainUser.toFindUserByIdResponse()

        // THEN
        assertEquals(expected, actual)
    }

    @Test
    fun `should map DomainUser to UpdateUserResponse with success`() {
        // GIVEN
        val expected = UpdateUserResponse.newBuilder().apply { successBuilder.user = exampleProtoUser }.build()

        // WHEN
        val actual = exampleDomainUser.toUpdateUserResponse()

        // THEN
        assertEquals(expected, actual)
    }

    @Test
    fun `should create DeleteUserByIdResponse with success`() {
        // WHEN
        val response = toDeleteUserByIdResponse()

        // THEN
        assertTrue(response.hasSuccess(), "Response should contain success")
    }
}
