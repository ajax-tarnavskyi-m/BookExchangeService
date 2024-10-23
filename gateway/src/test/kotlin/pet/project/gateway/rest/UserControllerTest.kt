package pet.project.gateway.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import pet.project.gateway.client.NatsClient
import pet.project.gateway.dto.user.CreateUserExternalRequest
import pet.project.gateway.dto.user.UpdateUserExternalRequest
import pet.project.gateway.dto.user.UserExternalResponse
import pet.project.gateway.mapper.UserRequestMapper.toDeleteUserByIdRequest
import pet.project.gateway.mapper.UserRequestMapper.toFindUserByIdRequest
import pet.project.gateway.mapper.UserRequestMapper.toProto
import pet.project.gateway.mapper.UserRequestMapper.toUpdateUserRequest
import pet.project.internal.app.subject.NatsSubject
import pet.project.internal.commonmodels.user.User
import pet.project.internal.input.reqreply.user.AddBookToUsersWishListRequest
import pet.project.internal.input.reqreply.user.AddBookToUsersWishListResponse
import pet.project.internal.input.reqreply.user.CreateUserResponse
import pet.project.internal.input.reqreply.user.DeleteUserByIdResponse
import pet.project.internal.input.reqreply.user.FindUserByIdResponse
import pet.project.internal.input.reqreply.user.UpdateUserResponse
import reactor.kotlin.core.publisher.toMono

@WebFluxTest(UserController::class)
class UserControllerTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var natsClient: NatsClient

    private val exampleUser = User.newBuilder()
        .setId(ObjectId.get().toHexString())
        .setLogin("testUser")
        .setEmail("test.user@example.com")
        .addAllBookWishList(emptySet())
        .build()

    private val exampleUserResponse =
        UserExternalResponse(exampleUser.id, exampleUser.login, exampleUser.email, exampleUser.bookWishListList.toSet())

    @Test
    fun `should create user successfully`() {
        // GIVEN
        val createUserRequest = CreateUserExternalRequest("testUser", "test.user@example.com", emptySet())

        val createUserResponse = CreateUserResponse.newBuilder().apply { successBuilder.user = exampleUser }.build()

        every {
            natsClient.doRequest(NatsSubject.User.CREATE, createUserRequest.toProto(), CreateUserResponse.parser())
        } returns createUserResponse.toMono()

        // WHEN & THEN
        webTestClient.post()
            .uri("/user")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(createUserRequest)
            .exchange()
            .expectStatus().isCreated
            .expectBody<UserExternalResponse>()
            .isEqualTo(exampleUserResponse)

        verify {
            natsClient.doRequest(NatsSubject.User.CREATE, createUserRequest.toProto(), CreateUserResponse.parser())
        }
    }

    @Test
    fun `should get user by id successfully`() {
        // GIVEN
        val findUserResponse = FindUserByIdResponse.newBuilder()
            .apply { successBuilder.user = exampleUser }.build()
        every {
            natsClient.doRequest(
                NatsSubject.User.FIND_BY_ID,
                toFindUserByIdRequest(exampleUser.id),
                FindUserByIdResponse.parser()
            )
        } returns findUserResponse.toMono()

        // WHEN & THEN
        webTestClient.get()
            .uri("/user/{id}", exampleUser.id)
            .exchange()
            .expectStatus().isOk
            .expectBody<UserExternalResponse>()
            .isEqualTo(exampleUserResponse)

        verify {
            natsClient.doRequest(
                NatsSubject.User.FIND_BY_ID,
                toFindUserByIdRequest(exampleUser.id),
                FindUserByIdResponse.parser()
            )
        }
    }

    @Test
    fun `should add book to wishlist successfully`() {
        // GIVEN
        val bookId = ObjectId.get().toHexString()
        val request = AddBookToUsersWishListRequest.newBuilder().setBookId(bookId).setUserId(exampleUser.id).build()
        val response = AddBookToUsersWishListResponse.newBuilder().apply { successBuilder }.build()
        every {
            natsClient.doRequest(
                NatsSubject.User.ADD_BOOK_TO_WISH_LIST,
                request,
                AddBookToUsersWishListResponse.parser()
            )
        } returns response.toMono()

        // WHEN & THEN
        webTestClient.put()
            .uri { builder -> builder.path("/user/{id}/wishlist").queryParam("bookId", bookId).build(exampleUser.id) }
            .exchange()
            .expectStatus().isNoContent

        // THEN
        verify {
            natsClient.doRequest(
                NatsSubject.User.ADD_BOOK_TO_WISH_LIST,
                request,
                AddBookToUsersWishListResponse.parser()
            )
        }
    }

    @Test
    fun `should update user successfully`() {
        // GIVEN
        val request =
            UpdateUserExternalRequest(exampleUser.login, exampleUser.email, exampleUser.bookWishListList.toSet())
        val response = UpdateUserResponse.newBuilder().apply { successBuilder.user = exampleUser }.build()
        every {
            natsClient.doRequest(
                NatsSubject.User.UPDATE,
                toUpdateUserRequest(exampleUser.id, request),
                UpdateUserResponse.parser()
            )
        } returns response.toMono()

        // WHEN & THEN
        webTestClient.put()
            .uri("/user/{id}", exampleUser.id)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody<UserExternalResponse>()
            .isEqualTo(exampleUserResponse)

        // THEN
        verify {
            natsClient.doRequest(
                NatsSubject.User.UPDATE,
                toUpdateUserRequest(exampleUser.id, request),
                UpdateUserResponse.parser()
            )
        }
    }

    @Test
    fun `should delete user successfully`() {
        // GIVEN
        val response = DeleteUserByIdResponse.newBuilder().apply { successBuilder }.build()
        every {
            natsClient.doRequest(
                NatsSubject.User.DELETE,
                toDeleteUserByIdRequest(exampleUser.id),
                DeleteUserByIdResponse.parser()
            )
        } returns response.toMono()

        // WHEN & THEN
        webTestClient.delete()
            .uri("/user/{id}", exampleUser.id)
            .exchange()
            .expectStatus().isNoContent

        // THEN
        verify {
            natsClient.doRequest(
                NatsSubject.User.DELETE,
                toDeleteUserByIdRequest(exampleUser.id),
                DeleteUserByIdResponse.parser()
            )
        }
    }
}
