package pet.project.gateway.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import pet.project.gateway.client.NatsClient
import pet.project.gateway.dto.user.CreateUserExternalRequest
import pet.project.gateway.dto.user.UpdateUserExternalRequest
import pet.project.gateway.dto.user.UserExternalResponse
import pet.project.gateway.mapper.UserRequestProtoMapper.toDeleteUserByIdRequest
import pet.project.gateway.mapper.UserRequestProtoMapper.toFindUserByIdRequest
import pet.project.gateway.mapper.UserRequestProtoMapper.toProto
import pet.project.gateway.mapper.UserRequestProtoMapper.toUpdateUserRequest
import pet.project.internal.app.subject.UserNatsSubject
import pet.project.internal.commonmodels.user.user.User
import pet.project.internal.input.reqreply.user.add_book_to_wish_list.AddBookToUsersWishListRequest
import pet.project.internal.input.reqreply.user.add_book_to_wish_list.AddBookToUsersWishListResponse
import pet.project.internal.input.reqreply.user.create.CreateUserResponse
import pet.project.internal.input.reqreply.user.delete.DeleteUserByIdResponse
import pet.project.internal.input.reqreply.user.find.FindUserByIdResponse
import pet.project.internal.input.reqreply.user.update.UpdateUserRequest.WishListUpdate
import pet.project.internal.input.reqreply.user.update.UpdateUserResponse
import reactor.kotlin.core.publisher.toMono

@WebFluxTest(UserController::class)
class UserControllerTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var natsClient: NatsClient

    private val dummyWishlist = setOf(
        "66bf6bf8039339103054e21a",
        "66c3636647ff4c2f0242073d",
        "66c3637847ff4c2f0242073e"
    )

    private val exampleUser = User.newBuilder()
        .setId("66bf6bf8039339103054e21a")
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
            natsClient.doRequest(
                "${UserNatsSubject.PREFIX}.${UserNatsSubject.CREATE}",
                createUserRequest.toProto(),
                CreateUserResponse.parser()
            )
        } returns createUserResponse.toMono()

        // WHEN & THEN
        webTestClient.post()
            .uri("/user")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(createUserRequest)
            .exchange()
            .expectStatus().isCreated
            .expectBody(UserExternalResponse::class.java)
            .isEqualTo(exampleUserResponse)

        verify {
            natsClient.doRequest(
                "${UserNatsSubject.PREFIX}.${UserNatsSubject.CREATE}",
                createUserRequest.toProto(),
                CreateUserResponse.parser()
            )
        }
    }

    @Test
    fun `should get user by id successfully`() {
        // GIVEN
        val findUserResponse = FindUserByIdResponse.newBuilder()
            .apply { successBuilder.user = exampleUser }.build()
        every {
            natsClient.doRequest(
                "${UserNatsSubject.PREFIX}.${UserNatsSubject.FIND_BY_ID}",
                toFindUserByIdRequest(exampleUser.id),
                FindUserByIdResponse.parser()
            )
        } returns findUserResponse.toMono()

        // WHEN & THEN
        webTestClient.get()
            .uri("/user/{id}", exampleUser.id)
            .exchange()
            .expectStatus().isOk
            .expectBody(UserExternalResponse::class.java)
            .isEqualTo(exampleUserResponse)

        verify {
            natsClient.doRequest(
                "${UserNatsSubject.PREFIX}.${UserNatsSubject.FIND_BY_ID}",
                toFindUserByIdRequest(exampleUser.id),
                FindUserByIdResponse.parser()
            )
        }
    }

    @Test
    fun `should add book to wishlist successfully`() {
        // GIVEN
        val bookId = "66bf6bf8039339103054e21a"
        val request = AddBookToUsersWishListRequest.newBuilder().setBookId(bookId).setUserId(exampleUser.id).build()
        val response = AddBookToUsersWishListResponse.newBuilder().apply { successBuilder }.build()
        every {
            natsClient.doRequest(
                "${UserNatsSubject.PREFIX}.${UserNatsSubject.ADD_BOOK_TO_WISH_LIST}",
                request,
                AddBookToUsersWishListResponse.parser()
            )
        } returns response.toMono()


        //addBookToWishList(userId, bookId) } returns Unit.toMono()

        // WHEN & THEN
        webTestClient.put()
            .uri { builder ->
                builder.path("/user/{id}/wishlist")
                    .queryParam("bookId", bookId)
                    .build(exampleUser.id)
            }
            .exchange()
            .expectStatus().isNoContent

        // THEN
        verify {
            natsClient.doRequest(
                "${UserNatsSubject.PREFIX}.${UserNatsSubject.ADD_BOOK_TO_WISH_LIST}",
                request,
                AddBookToUsersWishListResponse.parser()
            )
        }
    }

    @Test
    fun `should update user successfully`() {
        // GIVEN
        val wishListUpdate = WishListUpdate.newBuilder().addAllBookIds(exampleUser.bookWishListList)
        val request =
            UpdateUserExternalRequest(exampleUser.login, exampleUser.email, exampleUser.bookWishListList.toSet())
        val response = UpdateUserResponse.newBuilder().apply { successBuilder.user = exampleUser }.build()
        every {
            natsClient.doRequest(
                "${UserNatsSubject.PREFIX}.${UserNatsSubject.UPDATE}",
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
            .expectBody(UserExternalResponse::class.java)
            .isEqualTo(exampleUserResponse)

        // THEN
        verify {
            natsClient.doRequest(
                "${UserNatsSubject.PREFIX}.${UserNatsSubject.UPDATE}",
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
                "${UserNatsSubject.PREFIX}.${UserNatsSubject.DELETE}",
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
                "${UserNatsSubject.PREFIX}.${UserNatsSubject.DELETE}",
                toDeleteUserByIdRequest(exampleUser.id),
                DeleteUserByIdResponse.parser()
            )
        }
    }
}

