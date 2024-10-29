package pet.project.app.mapper

import org.junit.jupiter.api.Assertions.assertEquals
import pet.project.app.mapper.UserResponseMapper.generateSuccessfulAddBookToUserWishListResponse
import pet.project.app.mapper.UserResponseMapper.generateSuccessfulDeleteUserByIdResponse
import pet.project.app.mapper.UserResponseMapper.toCreateUserResponse
import pet.project.app.mapper.UserResponseMapper.toFindUserByIdResponse
import pet.project.app.mapper.UserResponseMapper.toUpdateUserResponse
import pet.project.app.model.domain.DomainUser
import pet.project.core.RandomTestFields.Book.randomBookIdString
import pet.project.core.RandomTestFields.User.randomEmail
import pet.project.core.RandomTestFields.User.randomLogin
import pet.project.core.RandomTestFields.User.randomUserIdString
import pet.project.internal.commonmodels.user.User
import pet.project.internal.input.reqreply.user.CreateUserResponse
import pet.project.internal.input.reqreply.user.FindUserByIdResponse
import pet.project.internal.input.reqreply.user.UpdateUserResponse
import kotlin.test.Test
import kotlin.test.assertTrue

class UserResponseMapperTest {

    private val exampleDomainUser = DomainUser(
        randomUserIdString(),
        randomLogin(),
        randomEmail(),
        setOf(randomBookIdString())
    )

    private val exampleProtoUser = User.newBuilder()
        .setId(exampleDomainUser.id)
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
        val response = generateSuccessfulAddBookToUserWishListResponse()

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
        val response = generateSuccessfulDeleteUserByIdResponse()

        // THEN
        assertTrue(response.hasSuccess(), "Response should contain success")
    }
}
