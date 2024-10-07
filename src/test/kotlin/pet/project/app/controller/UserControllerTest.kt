package pet.project.app.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pet.project.app.dto.user.CreateUserRequest
import pet.project.app.dto.user.ResponseUserDto
import pet.project.app.dto.user.UpdateUserRequest
import pet.project.app.mapper.UserMapper
import pet.project.app.model.domain.DomainUser
import pet.project.app.service.UserService

@WebMvcTest(UserController::class)
@Import(UserMapper::class)
class UserControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var userService: UserService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val dummyWishlist = setOf(
        "66bf6bf8039339103054e21a",
        "66c3636647ff4c2f0242073d",
        "66c3637847ff4c2f0242073e"
    )

    @Test
    fun `create user successfully`() {
        // GIVEN
        val createUserRequest = CreateUserRequest("testUser", "test.user@example.com", emptySet())
        val initializedDomainUser =
            DomainUser("66bf6bf8039339103054e21a", "testUser", "test.user@example.com", emptySet())
        every { userService.create(createUserRequest) } returns initializedDomainUser

        // WHEN
        val result = mockMvc.perform(
            post("/user/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest))
        )
            .andExpect(status().isCreated)
            .andReturn()

        // THEN
        val actual = objectMapper.readValue(result.response.contentAsString, ResponseUserDto::class.java)
        val expected = ResponseUserDto("66bf6bf8039339103054e21a", "testUser", "test.user@example.com", emptySet())
        assertEquals(expected, actual)
        verify { userService.create(createUserRequest) }
    }

    @Test
    fun `get user by id successfully`() {
        // GIVEN
        val domainUser = DomainUser("66c35b050da7b9523070cb3a", "testUser", "test.user@example.com", emptySet())
        every { userService.getById("66c35b050da7b9523070cb3a") } returns domainUser

        // WHEN
        val result = mockMvc.perform(get("/user/{id}", "66c35b050da7b9523070cb3a"))
            .andExpect(status().isOk)
            .andReturn()

        // THEN
        val actual = objectMapper.readValue(result.response.contentAsString, ResponseUserDto::class.java)
        val expected = ResponseUserDto("66c35b050da7b9523070cb3a", "testUser", "test.user@example.com", emptySet())
        assertEquals(expected, actual)
        verify { userService.getById("66c35b050da7b9523070cb3a") }
    }

    @Test
    fun `update user successfully`() {
        // GIVEN
        val userId = "66c35b050da7b9523070cb3a"
        val updateUserRequest = UpdateUserRequest("updatedUser", "test.user@example.com", dummyWishlist)
        val mappedDomainUser = DomainUser(userId, "updatedUser", "test.user@example.com", dummyWishlist)
        every { userService.update(userId, updateUserRequest) } returns mappedDomainUser

        // WHEN
        val result = mockMvc.perform(
            put("/user/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserRequest))
        )
            .andExpect(status().isOk)
            .andReturn()

        // THEN
        val actual = objectMapper.readValue(result.response.contentAsString, ResponseUserDto::class.java)
        val expected = ResponseUserDto(userId, "updatedUser", "test.user@example.com", dummyWishlist)
        assertEquals(expected, actual)
        verify { userService.update(userId, updateUserRequest) }
    }

    @Test
    fun `add book to wishlist successfully`() {
        // GIVEN
        val userId = "66c35b050da7b9523070cb3a"
        val bookId = "66bf6bf8039339103054e21a"
        every { userService.addBookToWishList(userId, bookId) } returns true

        // WHEN
        mockMvc.perform(
            put("/user/{id}/wishlist", userId)
                .param("bookId", bookId)
        )
            .andExpect(status().isNoContent)

        // THEN
        verify { userService.addBookToWishList(userId, bookId) }
    }

    @Test
    fun `delete user successfully`() {
        // GIVEN
        val userId = "66c35b050da7b9523070cb3a"
        every { userService.delete(userId) } just runs

        // WHEN
        mockMvc.perform(delete("/user/{id}", userId))
            .andExpect(status().isNoContent)

        // THEN
        verify { userService.delete(userId) }
    }

}
