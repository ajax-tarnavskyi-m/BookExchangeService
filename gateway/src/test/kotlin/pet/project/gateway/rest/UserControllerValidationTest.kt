package pet.project.gateway.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.web.method.annotation.HandlerMethodValidationException
import pet.project.core.RandomTestFields.Book.bookIdString
import pet.project.core.RandomTestFields.User.email
import pet.project.core.RandomTestFields.User.login
import pet.project.core.RandomTestFields.User.userIdString
import pet.project.core.exception.handler.GlobalExceptionHandler
import pet.project.core.exception.handler.ValidationExceptionResponse
import pet.project.gateway.client.NatsClient
import pet.project.gateway.dto.user.CreateUserExternalRequest
import pet.project.gateway.dto.user.UpdateUserExternalRequest
import pet.project.internal.input.reqreply.user.AddBookToUsersWishListRequest
import pet.project.internal.input.reqreply.user.AddBookToUsersWishListResponse
import pet.project.internal.input.reqreply.user.CreateUserRequest
import pet.project.internal.input.reqreply.user.CreateUserResponse
import pet.project.internal.input.reqreply.user.DeleteUserByIdRequest
import pet.project.internal.input.reqreply.user.DeleteUserByIdResponse
import pet.project.internal.input.reqreply.user.UpdateUserRequest
import pet.project.internal.input.reqreply.user.UpdateUserResponse

@WebMvcTest(UserController::class)
@Import(GlobalExceptionHandler::class)
class UserControllerValidationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var natsClient: NatsClient

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `should return bad request when creating user with empty login`() {
        // GIVEN
        val request = CreateUserExternalRequest("", email)

        // WHEN
        val result = mockMvc.perform(
            post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andReturn()

        // THEN
        val response = objectMapper.readValue(result.response.contentAsString, ValidationExceptionResponse::class.java)
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status)
        assertEquals(1, response.invalidInputReports.size, "should have only one validation exception report")
        assertEquals("login", response.invalidInputReports[0].field)
        assertEquals("User login must not be blank", response.invalidInputReports[0].message)
        verify(exactly = 0) { natsClient.doRequest<CreateUserRequest, CreateUserResponse>(any(), any(), any()) }
    }

    @Test
    fun `should return bad request when updating user with invalid ObjectId`() {
        // GIVEN
        val request = UpdateUserExternalRequest(login, email, setOf())
        val invalidUserId = "invalidUserId"

        // WHEN
        val result = mockMvc.perform(
            put("/user/{id}", invalidUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andReturn()

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.response.status)
        val exception = result.resolvedException as HandlerMethodValidationException
        val actualMessage = exception.detailMessageArguments[0]
        assertEquals("The provided ID must be a valid ObjectId hex String", actualMessage)
        verify(exactly = 0) { natsClient.doRequest<UpdateUserRequest, UpdateUserResponse>(any(), any(), any()) }
    }

    @Test
    fun `should return bad request when adding book to wishlist with invalid user ObjectId`() {
        // GIVEN
        val invalidUserId = "invalidObjectId"

        // WHEN
        val result = mockMvc.perform(
            put("/user/{id}/wishlist", invalidUserId)
                .param("bookId", bookIdString)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn()

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.response.status)
        val exception = result.resolvedException as HandlerMethodValidationException
        val actualMessage = exception.detailMessageArguments[0]
        assertEquals("The provided ID must be a valid ObjectId hex String", actualMessage)
        verify(exactly = 0) {
            natsClient.doRequest<AddBookToUsersWishListRequest, AddBookToUsersWishListResponse>(any(), any(), any())
        }
    }

    @Test
    fun `should return bad request when adding book to wishlist with invalid book ObjectId`() {
        // GIVEN
        val invalidBookId = "invalidObjectId"

        // WHEN
        val result = mockMvc.perform(
            put("/user/{id}/wishlist", userIdString)
                .param("bookId", invalidBookId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn()

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.response.status)
        val exception = result.resolvedException as HandlerMethodValidationException
        val actualMessage = exception.detailMessageArguments[0]
        assertEquals("The provided ID must be a valid ObjectId hex String", actualMessage)
        verify(exactly = 0) {
            natsClient.doRequest<AddBookToUsersWishListRequest, AddBookToUsersWishListResponse>(any(), any(), any())
        }
    }

    @Test
    fun `should return bad request when deleting user with invalid ObjectId`() {
        // GIVEN
        val invalidObjectId = "invalidObjectId"

        // WHEN
        val result = mockMvc.perform(
            delete("/user/{id}", invalidObjectId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn()

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.response.status)
        val exception = result.resolvedException as HandlerMethodValidationException
        val actualMessage = exception.detailMessageArguments[0]
        assertEquals("The provided ID must be a valid ObjectId hex String", actualMessage)
        verify(exactly = 0) {
            natsClient.doRequest<DeleteUserByIdRequest, DeleteUserByIdResponse>(any(), any(), any())
        }
    }

    @Test
    fun `should return bad request when getting user with invalid ObjectId`() {
        // GIVEN
        val invalidObjectId = "invalidObjectId"

        // WHEN
        val result = mockMvc.perform(
            get("/user/{id}", invalidObjectId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn()

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.response.status)
        val exception = result.resolvedException as HandlerMethodValidationException
        val actualMessage = exception.detailMessageArguments[0]
        assertEquals("The provided ID must be a valid ObjectId hex String", actualMessage)
        verify(exactly = 0) { natsClient.doRequest<DeleteUserByIdRequest, DeleteUserByIdResponse>(any(), any(), any()) }
    }
}
