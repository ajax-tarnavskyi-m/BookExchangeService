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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.book.ResponseBookDto
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.dto.book.UpdateBookRequest
import pet.project.app.mapper.BookMapper
import pet.project.app.model.domain.DomainBook
import pet.project.app.service.BookService
import java.math.BigDecimal

@WebMvcTest(BookController::class)
@Import(BookMapper::class)
class BookControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var bookService: BookService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `should return book details when book id is valid`() {
        // GIVEN
        val bookId = "66bf6bf8039339103054e21a"
        val initializedDomainBook = DomainBook(bookId, "Title", "Description", 2023, BigDecimal(20.0), 10)
        every { bookService.getById(bookId) } returns initializedDomainBook

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
    fun `should create book successfully when request is valid`() {
        // GIVEN
        val createBookRequest = CreateBookRequest("Title", "Description", 2023, BigDecimal(20.0), 10)
        val bookId = "66bf6bf8039339103054e21a"
        val initializedDomainBook = DomainBook(bookId, "Title", "Description", 2023, BigDecimal(20.0), 10)
        every { bookService.create(createBookRequest) } returns initializedDomainBook

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
        verify { bookService.create(createBookRequest) }
    }

    @Test
    fun `should update book details when request is valid`() {
        // GIVEN
        val bookId = "66bf6bf8039339103054e21a"
        val updateBookRequest = UpdateBookRequest("Title", "Description", 2023, BigDecimal(20.0))
        val updatedDomainBook = DomainBook(bookId, "Title", "Description", 2023, BigDecimal(20.0), 10)
        every { bookService.update(bookId, updateBookRequest) } returns updatedDomainBook

        // WHEN
        val result = mockMvc.perform(
            put("/book/{id}", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateBookRequest))
        )
            .andExpect(status().isOk)
            .andReturn()

        // THEN
        val actual = objectMapper.readValue(result.response.contentAsString, ResponseBookDto::class.java)
        val expected = ResponseBookDto(bookId, "Title", "Description", 2023, BigDecimal(20.0), 10)
        assertEquals(expected, actual)
        verify { bookService.update(bookId, updateBookRequest)}
        }


    @Test
    fun `should update book amount when request is valid`() {
        // GIVEN
        val bookId = "66bf6bf8039339103054e21a"
        val updateAmountRequest = UpdateAmountRequest(bookId, 5)

        every { bookService.updateAmount(updateAmountRequest) } returns true

        // WHEN
        mockMvc.perform(
            patch("/book/amount")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAmountRequest))
        )
            .andExpect(status().isNoContent)

        // THEN
        verify { bookService.updateAmount(updateAmountRequest) }
    }

    @Test
    fun `should delete book when book id is valid`() {
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
