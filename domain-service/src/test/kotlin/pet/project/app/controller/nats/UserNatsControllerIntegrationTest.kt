package pet.project.app.controller.nats

import io.nats.client.Connection
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.mapper.UserResponseMapper.toUpdateUserResponse
import pet.project.app.model.domain.DomainUser
import pet.project.app.repository.BookRepository
import pet.project.app.repository.UserRepository
import pet.project.internal.app.subject.NatsSubject
import pet.project.internal.commonmodels.user.User
import pet.project.internal.input.reqreply.user.AddBookToUsersWishListRequest
import pet.project.internal.input.reqreply.user.AddBookToUsersWishListResponse
import pet.project.internal.input.reqreply.user.CreateUserRequest
import pet.project.internal.input.reqreply.user.CreateUserResponse
import pet.project.internal.input.reqreply.user.DeleteUserByIdRequest
import pet.project.internal.input.reqreply.user.DeleteUserByIdResponse
import pet.project.internal.input.reqreply.user.FindUserByIdRequest
import pet.project.internal.input.reqreply.user.FindUserByIdResponse
import pet.project.internal.input.reqreply.user.UpdateUserRequest
import pet.project.internal.input.reqreply.user.UpdateUserResponse
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("test")
class UserNatsControllerIntegrationTest {

    @Autowired
    lateinit var natsConnection: Connection

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var bookRepository: BookRepository

    private val createBookRequest = CreateBookRequest(
        "Test book",
        "Test Description",
        2022,
        BigDecimal("19.99"),
        10,
    )

    private val exampleUser = User.newBuilder().setLogin("exampleUser").setEmail("example@mail.com").build()

    private val createUserRequest = CreateUserRequest.newBuilder()
        .setLogin(exampleUser.login)
        .setEmail(exampleUser.email)
        .build()

    @Test
    fun `should return CreateUserResponse when user is created successfully`() {
        // WHEN
        val responseMessage = natsConnection.request(NatsSubject.User.CREATE, createUserRequest.toByteArray()).get()

        // THEN
        val createUserResponse = CreateUserResponse.parser().parseFrom(responseMessage.data)
        assertTrue(createUserResponse.hasSuccess(), "Must successfully deliver NATS message")
        val responseUser = createUserResponse.success.user

        val actualSavedUser = userRepository.findById(responseUser.id).block()
        assertNotNull(actualSavedUser, "Should save user with id matching CreateUserResponse.id")
        val expectedUser = DomainUser(responseUser.id, responseUser.login, responseUser.email, setOf())
        assertEquals(expectedUser, actualSavedUser)
    }

    @Test
    fun `should return FindUserByIdResponse when user is found`() {
        // GIVEN
        val savedUser = userRepository.insert(createUserRequest).block()!!
        val request = FindUserByIdRequest.newBuilder().setId(savedUser.id).build()
        val expectedUser = User.newBuilder().apply {
            id = savedUser.id
            login = savedUser.login
            email = savedUser.email
        }.build()

        // WHEN
        val responseMessage = natsConnection.request(NatsSubject.User.FIND_BY_ID, request.toByteArray()).get()

        // THEN
        val findUserByIdResponse = FindUserByIdResponse.parser().parseFrom(responseMessage.data)
        assertTrue(findUserByIdResponse.hasSuccess(), "Must successfully deliver NATS message")
        assertEquals(expectedUser, findUserByIdResponse.success.user)
    }

    @Test
    fun `should return AddBookToUsersWishListResponse when book is successfully added`() {
        // GIVEN
        val savedUser = userRepository.insert(createUserRequest).block()!!
        val savedBook = bookRepository.insert(createBookRequest).block()!!
        val request = AddBookToUsersWishListRequest.newBuilder()
            .setUserId(savedUser.id)
            .setBookId(savedBook.id)
            .build()

        // WHEN
        val response = natsConnection.request(NatsSubject.User.ADD_BOOK_TO_WISH_LIST, request.toByteArray()).get()

        // THEN
        val parsedResponse = AddBookToUsersWishListResponse.parser().parseFrom(response.data)
        assertTrue(parsedResponse.hasSuccess(), "Must successfully deliver NATS message")

        val actualUpdatedUser = userRepository.findById(savedUser.id).block()!!
        val expectedUser = savedUser.copy(bookWishList = setOf(savedBook.id))
        assertEquals(expectedUser, actualUpdatedUser)
    }

    @Test
    fun `should return UpdateUserResponse when user is updated successfully`() {
        // GIVEN
        val savedUser = userRepository.insert(createUserRequest).block()!!
        val updateRequest = UpdateUserRequest.newBuilder()
            .setId(savedUser.id)
            .setLogin("updated_login")
            .build()
        val updatedUser = savedUser.copy(login = "updated_login")
        val expectedResponse = updatedUser.toUpdateUserResponse()

        // WHEN
        val responseMessage = natsConnection.request(NatsSubject.User.UPDATE, updateRequest.toByteArray()).get()

        // THEN
        val parsedActualResponse = UpdateUserResponse.parser().parseFrom(responseMessage.data)
        assertEquals(expectedResponse, parsedActualResponse)

        val actualUser = userRepository.findById(savedUser.id).block()!!
        assertEquals(updatedUser, actualUser)
    }

    @Test
    fun `should return DeleteUserByIdResponse when user is deleted successfully`() {
        // GIVEN
        val savedUser = userRepository.insert(createUserRequest).block()!!
        val deleteRequest = DeleteUserByIdRequest.newBuilder().setId(savedUser.id).build()

        // WHEN
        val responseMessage = natsConnection.request(NatsSubject.User.DELETE, deleteRequest.toByteArray()).get()

        // THEN
        val parsedResponse = DeleteUserByIdResponse.parser().parseFrom(responseMessage.data)
        assertTrue(parsedResponse.hasSuccess(), "Must successfully deliver NATS message")

        val foundUser = userRepository.findById(savedUser.id).block()
        assertNull(foundUser, "User should not be found after deletion")
    }
}
