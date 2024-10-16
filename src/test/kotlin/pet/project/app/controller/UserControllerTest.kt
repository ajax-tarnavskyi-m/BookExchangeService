package pet.project.app.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import pet.project.app.dto.user.CreateUserRequest
import pet.project.app.dto.user.ResponseUserDto
import pet.project.app.dto.user.UpdateUserRequest
import pet.project.app.mapper.UserMapper.toDto
import pet.project.app.model.domain.DomainUser
import pet.project.app.service.UserService
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@WebFluxTest(UserController::class)
class UserControllerTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var userService: UserService

    private val dummyWishlist = setOf(
        "66bf6bf8039339103054e21a",
        "66c3636647ff4c2f0242073d",
        "66c3637847ff4c2f0242073e"
    )

    @Test
    fun `should create user successfully`() {
        // GIVEN
        val createUserRequest = CreateUserRequest("testUser", "test.user@example.com", emptySet())
        val user = DomainUser("66bf6bf8039339103054e21a", "testUser", "test.user@example.com", emptySet())
        every { userService.create(createUserRequest) } returns user.toMono()
        val expectedResponse = user.toDto()

        // WHEN & THEN
        webTestClient.post()
            .uri("/user/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(createUserRequest)
            .exchange()
            .expectStatus().isCreated
            .expectBody(ResponseUserDto::class.java)
            .isEqualTo(expectedResponse)
        verify { userService.create(createUserRequest) }
    }

    @Test
    fun `should get user by id successfully`() {
        // GIVEN
        val userId = "66c35b050da7b9523070cb3a"
        val domainUser = DomainUser(userId, "testUser", "test.user@example.com", emptySet())
        every { userService.getById(userId) } returns domainUser.toMono()
        val expectedResponse = domainUser.toDto()

        // WHEN & THEN
        webTestClient.get()
            .uri("/user/{id}", userId)
            .exchange()
            .expectStatus().isOk
            .expectBody(ResponseUserDto::class.java)
            .isEqualTo(expectedResponse)
        verify { userService.getById(userId) }
    }

    @Test
    fun `should update user successfully`() {
        // GIVEN
        val userId = "66c35b050da7b9523070cb3a"
        val updateUserRequest = UpdateUserRequest("updatedUser", "test.user@example.com", dummyWishlist)
        val mappedDomainUser = DomainUser(userId, "updatedUser", "test.user@example.com", dummyWishlist)
        every { userService.update(userId, updateUserRequest) } returns mappedDomainUser.toMono()
        val expectedResponseUserDto = mappedDomainUser.toDto()

        // WHEN & THEN
        webTestClient.put()
            .uri("/user/{id}", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updateUserRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody(ResponseUserDto::class.java)
            .isEqualTo(expectedResponseUserDto)

        // THEN
        verify { userService.update(userId, updateUserRequest) }
    }

    @Test
    fun `should add book to wishlist successfully`() {
        // GIVEN
        val userId = "66c35b050da7b9523070cb3a"
        val bookId = "66bf6bf8039339103054e21a"
        every { userService.addBookToWishList(userId, bookId) } returns Unit.toMono()

        // WHEN & THEN
        webTestClient.put()
            .uri { builder ->
                builder.path("/user/{id}/wishlist")
                    .queryParam("bookId", bookId)
                    .build(userId)
            }
            .exchange()
            .expectStatus().isNoContent

        // THEN
        verify { userService.addBookToWishList(userId, bookId) }
    }

    @Test
    fun `should delete user successfully`() {
        // GIVEN
        val userId = "66c35b050da7b9523070cb3a"
        every { userService.delete(userId) } returns Unit.toMono()

        // WHEN & THEN
        webTestClient.delete()
            .uri("/user/{id}", userId)
            .exchange()
            .expectStatus().isNoContent

        // THEN
        verify { userService.delete(userId) }
    }

}
