package pet.project.app.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pet.project.app.dto.user.CreateUserRequest
import pet.project.app.dto.user.UpdateUserRequest
import pet.project.app.dto.user.UserMapper
import pet.project.app.service.UserService

@WebMvcTest(UserController::class)
@Import(UserMapper::class)
class UserControllerValidationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Suppress("unused")
    @MockkBean
    private lateinit var userServiceDependencyMockForInjection: UserService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `should return bad request when creating user with empty login`() {
        //GIVEN
        val request = CreateUserRequest("")

        //WHEN & THEN
        mockMvc.perform(
            post("/user/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return bad request when updating user with invalid ObjectId`() {
        //GIVEN
        val request = UpdateUserRequest("invalidObjectId", "UserLogin")

        //WHEN & THEN
        mockMvc.perform(
            put("/user/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return bad request when updating user with empty login`() {
        //GIVEN
        val request = UpdateUserRequest("507f191e810c19729de860ea", "")

        //WHEN & THEN
        mockMvc.perform(
            put("/user/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return bad request when adding book to wishlist with invalid user ObjectId`() {
        //GIVEN
        val invalidUserId = "invalidObjectId"
        val bookId = "507f191e810c19729de860ea"

        //WHEN & THEN
        mockMvc.perform(
            patch("/user/{id}/wishlist", invalidUserId)
                .param("bookId", bookId)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return bad request when adding book to wishlist with invalid book ObjectId`() {
        //GIVEN
        val userId = "507f191e810c19729de860ea"
        val invalidBookId = "invalidObjectId"

        //WHEN & THEN
        mockMvc.perform(
            patch("/user/{id}/wishlist", userId)
                .param("bookId", invalidBookId)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return bad request when deleting user with invalid ObjectId`() {
        //GIVEN
        val invalidObjectId = "invalidObjectId"

        //WHEN & THEN
        mockMvc.perform(
            delete("/user/{id}", invalidObjectId)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return bad request when getting user with invalid ObjectId`() {
        //GIVEN
        val invalidObjectId = "invalidObjectId"

        //WHEN & THEN
        mockMvc.perform(
            get("/user/{id}", invalidObjectId)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }
}
