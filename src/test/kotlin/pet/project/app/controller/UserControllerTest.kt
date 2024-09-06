package pet.project.app.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pet.project.app.dto.user.CreateUserRequest
import pet.project.app.dto.user.ResponseUserDto
import pet.project.app.dto.user.UpdateUserRequest
import pet.project.app.dto.user.UserMapper
import pet.project.app.model.User
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
        val createUserRequest = CreateUserRequest("testUser", emptySet())
        val mappedUser = User(null, "testUser", emptySet())
        val initializedUser = User(ObjectId("66bf6bf8039339103054e21a"), "testUser", emptySet())
        every { userService.create(mappedUser) } returns initializedUser

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
        val expected = ResponseUserDto("66bf6bf8039339103054e21a", "testUser", emptySet())
        assertEquals(expected, actual)
        verify { userService.create(mappedUser) }
    }

    @Test
    fun `get user by id successfully`() {
        // GIVEN
        val user = User(ObjectId("66c35b050da7b9523070cb3a"), "testUser", emptySet())

        every { userService.getById("66c35b050da7b9523070cb3a") } returns user

        // WHEN
        val result = mockMvc.perform(get("/user/{id}", "66c35b050da7b9523070cb3a"))
            .andExpect(status().isOk)
            .andReturn()

        // THEN
        val actual = objectMapper.readValue(result.response.contentAsString, ResponseUserDto::class.java)
        val expected = ResponseUserDto("66c35b050da7b9523070cb3a", "testUser", emptySet())
        assertEquals(expected, actual)
        verify { userService.getById("66c35b050da7b9523070cb3a") }
    }

    @Test
    fun `update user successfully`() {
        // GIVEN
        val updateUserRequest = UpdateUserRequest("66c35b050da7b9523070cb3a", "updatedUser", dummyWishlist)
        val mappedUser = User(ObjectId("66c35b050da7b9523070cb3a"), "updatedUser", dummyWishlist)
        every { userService.update(any()) } returns mappedUser

        // WHEN
        val result = mockMvc.perform(
            put("/user/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserRequest))
        )
            .andExpect(status().isOk)
            .andReturn()

        // THEN
        val actual = objectMapper.readValue(result.response.contentAsString, ResponseUserDto::class.java)
        val expected = ResponseUserDto("66c35b050da7b9523070cb3a", "updatedUser", dummyWishlist)
        assertEquals(expected, actual)
        verify { userService.update(any()) }
    }

    @Test
    fun `add book to wishlist successfully`() {
        // GIVEN
        val userId = "66c35b050da7b9523070cb3a"
        val bookId = "66bf6bf8039339103054e21a"
        val updatedUser = User(ObjectId(userId), "testUser", setOf(bookId))
        every { userService.addBookToWishList(userId, bookId) } returns updatedUser

        // WHEN
        mockMvc.perform(
            patch("/user/{id}/wishlist", userId)
                .param("bookId", bookId)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.bookWishList").value(bookId))

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
