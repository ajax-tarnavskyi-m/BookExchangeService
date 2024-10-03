package pet.project.app.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
import pet.project.app.dto.book.ResponseBookDto
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.dto.book.UpdateBookRequest
import pet.project.app.model.Book
import pet.project.app.service.BookService
import java.math.BigDecimal

@WebMvcTest(BookController::class)
@Import(BookMapper::class)
internal class BookControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var bookService: BookService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `get book by id successfully`() {
        // GIVEN
        val bookId = "66bf6bf8039339103054e21a"
        val initializedBook = Book(ObjectId(bookId), "Title", "Description", 2023, BigDecimal(20.0), 10)
        every { bookService.getById(bookId) } returns initializedBook

        // WHEN
        val result = mockMvc.perform(get("/book/{id}", bookId))
            .andExpect(status().isOk)
            .andReturn()

        // THEN
        val actual = objectMapper.readValue(result.response.contentAsString, ResponseBookDto::class.java)
        val expected = ResponseBookDto(bookId, "Title", "Description", 2023, BigDecimal(20.0), 10)
        assertEquals(expected, actual)
        verify { bookService.getById(bookId) }
    }

    @Test
    fun `create book successfully`() {
        // GIVEN
        val createBookRequest = CreateBookRequest("Title", "Description", 2023, BigDecimal(20.0), 10)
        val mappedBook = Book(null, "Title", "Description", 2023, BigDecimal(20.0), 10)
        val bookId = "66bf6bf8039339103054e21a"
        val initializedBook = Book(ObjectId(bookId), "Title", "Description", 2023, BigDecimal(20.0), 10)
        every { bookService.create(mappedBook) } returns initializedBook

        // WHEN
        val result = mockMvc.perform(
            post("/book/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createBookRequest))
        )
            .andExpect(status().isCreated)
            .andReturn()

        // THEN
        val actual = objectMapper.readValue(result.response.contentAsString, ResponseBookDto::class.java)
        val expected = ResponseBookDto(bookId, "Title", "Description", 2023, BigDecimal(20.0), 10)
        assertEquals(expected, actual)
        verify { bookService.create(mappedBook) }
    }

    @Test
    fun `update book successfully`() {
        // GIVEN
        val bookId = "66bf6bf8039339103054e21a"
        val updateBookRequest = UpdateBookRequest(bookId, "Title", "Description", 2023, BigDecimal(20.0), 10)
        val updatedBook = Book(ObjectId(bookId), "Title", "Description", 2023, BigDecimal(20.0), 10)
        every { bookService.update(updatedBook) } returns updatedBook

        // WHEN
        val result = mockMvc.perform(
            put("/book/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateBookRequest))
        )
            .andExpect(status().isOk)
            .andReturn()

        // THEN
        val actual = objectMapper.readValue(result.response.contentAsString, ResponseBookDto::class.java)
        val expected = ResponseBookDto(bookId, "Title", "Description", 2023, BigDecimal(20.0), 10)
        assertEquals(expected, actual)
        verify { bookService.update(any()) }
    }

    @Test
    fun `update book amount successfully`() {
        // GIVEN
        val bookId = "66bf6bf8039339103054e21a"
        val updateAmountRequest = UpdateAmountRequest(bookId,5 )

        every { bookService.updateAmount(updateAmountRequest) } returns true

        // WHEN
        val result = mockMvc.perform(
            patch("/book/amount")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAmountRequest))
        )
            .andExpect(status().isOk)
            .andReturn()

        // THEN
        val resultValue = objectMapper.readValue(result.response.contentAsString, Boolean::class.java)
        assertTrue(resultValue)
        verify { bookService.updateAmount(updateAmountRequest) }
    }

    @Test
    fun `delete book successfully`() {
        // GIVEN
        val bookId = "66bf6bf8039339103054e21a"

        every { bookService.delete(bookId) } just runs

        // WHEN
        mockMvc.perform(delete("/book/{id}", bookId))
            .andExpect(status().isNoContent)

        // THEN
        verify { bookService.delete(bookId) }
    }
}
