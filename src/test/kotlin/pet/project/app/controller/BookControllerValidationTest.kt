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
import pet.project.app.dto.book.BookMapper
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.dto.book.UpdateBookRequest
import pet.project.app.service.BookService

@WebMvcTest(BookController::class)
@Import(BookMapper::class)
class BookControllerValidationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var bookService: BookService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `should return bad request when creating book with empty title`() {
        //GIVEN
        val request = CreateBookRequest("", "Description", 2020, 19.99, 10)

        //WHEN & THEN
        mockMvc.perform(
            post("/book/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return bad request when creating book with invalid year`() {
        //GIVEN
        val request = CreateBookRequest("Title", "Description", 1500, 19.99, 10)

        //WHEN & THEN
        mockMvc.perform(
            post("/book/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return bad request when creating book with negative price`() {
        //GIVEN
        val request = CreateBookRequest("Title", "Description", 2020, -19.99, 10)

        //WHEN & THEN
        mockMvc.perform(
            post("/book/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return bad request when creating book with negative amountAvailable`() {
        //GIVEN
        val request = CreateBookRequest("Title", "Description", 2020, 19.99, -10)

        //WHEN & THEN
        mockMvc.perform(
            post("/book/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return bad request when updating book with invalid ObjectId`() {
        //GIVEN
        val request = UpdateBookRequest("invalidObjectId", "Title", "Description", 2020, 19.99, 10)

        //WHEN & THEN
        mockMvc.perform(
            put("/book/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return bad request when updating book with empty title`() {
        //GIVEN
        val request = UpdateBookRequest("507f191e810c19729de860ea", "", "Description", 2020, 19.99, 10)

        //WHEN & THEN
        mockMvc.perform(
            put("/book/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return bad request when updating book with zero delta`() {
        //GIVEN
        val request = UpdateAmountRequest(0)

        mockMvc.perform(
            patch("/book/{id}/amount", "507f191e810c19729de860ea")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return bad request when deleting book with invalid ObjectId`() {
        //GIVEN
        val invalidObjectId = "invalidObjectId"

        //WHEN & THEN
        mockMvc.perform(
            delete("/book/{id}", invalidObjectId)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return bad request when getting book with invalid ObjectId`() {
        //GIVEN
        val invalidObjectId = "invalidObjectId"

        //WHEN & THEN
        mockMvc.perform(
            get("/book/{id}", invalidObjectId)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }
}
