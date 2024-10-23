package pet.project.app.controller.nats

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import pet.project.app.mapper.UserResponseMapper.toAddBookToUserWishListResponse
import pet.project.app.mapper.UserResponseMapper.toCreateUserResponse
import pet.project.app.mapper.UserResponseMapper.toDeleteUserByIdResponse
import pet.project.app.mapper.UserResponseMapper.toFindUserByIdResponse
import pet.project.app.mapper.UserResponseMapper.toUpdateUserResponse
import pet.project.app.model.domain.DomainUser
import pet.project.app.service.UserService
import pet.project.internal.input.reqreply.user.AddBookToUsersWishListRequest
import pet.project.internal.input.reqreply.user.CreateUserRequest
import pet.project.internal.input.reqreply.user.DeleteUserByIdRequest
import pet.project.internal.input.reqreply.user.FindUserByIdRequest
import pet.project.internal.input.reqreply.user.UpdateUserRequest
import reactor.core.publisher.Mono
import reactor.kotlin.test.test

@ExtendWith(MockKExtension::class)
class UserNatsControllerTest {

    @MockK
    lateinit var userService: UserService

    @InjectMockKs
    lateinit var userNatsController: UserNatsController

    @Test
    fun `should return CreateUserResponse when user is created successfully`() {
        // GIVEN
        val request = CreateUserRequest.newBuilder()
            .setLogin("testLogin")
            .setEmail("test@example.com")
            .build()
        val domainUser = DomainUser(ObjectId.get().toHexString(), "testLogin", "test@example.com", setOf())
        val expectedResponse = domainUser.toCreateUserResponse()

        every { userService.create(request) } returns Mono.just(domainUser)

        // WHEN
        val result = userNatsController.create(request).test()

        // THEN
        result.expectNext(expectedResponse)
            .verifyComplete()

        verify(exactly = 1) { userService.create(request) }
    }

    @Test
    fun `should return FindUserByIdResponse when user is found`() {
        // GIVEN
        val userId = ObjectId.get().toHexString()
        val request = FindUserByIdRequest.newBuilder().setId(userId).build()
        val domainUser = DomainUser(userId, "testLogin", "test@example.com", setOf())
        val expectedResponse = domainUser.toFindUserByIdResponse()
        every { userService.getById(userId) } returns Mono.just(domainUser)

        // WHEN
        val result = userNatsController.getById(request).test()

        // THEN
        result
            .expectNext(expectedResponse)
            .verifyComplete()

        verify(exactly = 1) { userService.getById(userId) }
    }

    @Test
    fun `should return AddBookToUsersWishListResponse when book is successfully added`() {
        // GIVEN
        val userId = ObjectId.get().toHexString()
        val bookId = ObjectId.get().toHexString()

        val request = AddBookToUsersWishListRequest.newBuilder()
            .setUserId(userId)
            .setBookId(bookId)
            .build()

        every { userService.addBookToWishList(userId, bookId) } returns Mono.just(Unit)

        val expectedResponse = toAddBookToUserWishListResponse()

        // WHEN
        val result = userNatsController.addBookToWishList(request).test()

        // THEN
        result
            .expectNext(expectedResponse)
            .verifyComplete()

        verify(exactly = 1) { userService.addBookToWishList(userId, bookId) }
    }

    @Test
    fun `should return UpdateUserResponse when user is updated successfully`() {
        // GIVEN
        val userId = ObjectId.get().toHexString()
        val request = UpdateUserRequest.newBuilder()
            .setId(userId)
            .setLogin("newLogin")
            .setEmail("newEmail@example.com")
            .build()

        val domainUser = DomainUser(userId, "newLogin", "newEmail@example.com", setOf())
        val expectedResponse = domainUser.toUpdateUserResponse()

        every { userService.update(userId, request) } returns Mono.just(domainUser)

        // WHEN
        val result = userNatsController.update(request).test()

        // THEN
        result
            .expectNext(expectedResponse)
            .verifyComplete()

        verify(exactly = 1) { userService.update(userId, request) }
    }

    @Test
    fun `should return DeleteUserByIdResponse when user is deleted successfully`() {
        // GIVEN
        val userId = ObjectId.get().toHexString()
        val request = DeleteUserByIdRequest.newBuilder()
            .setId(userId)
            .build()

        val expectedResponse = toDeleteUserByIdResponse()

        every { userService.delete(userId) } returns Mono.just(Unit)

        // WHEN
        val result = userNatsController.delete(request).test()

        // THEN
        result
            .expectNext(expectedResponse)
            .verifyComplete()

        verify(exactly = 1) { userService.delete(userId) }
    }
}
