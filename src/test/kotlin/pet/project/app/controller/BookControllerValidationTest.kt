package pet.project.app.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.method.annotation.HandlerMethodValidationException
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.dto.book.UpdateBookRequest
import pet.project.app.exception.handler.ValidationExceptionResponse
import pet.project.app.mapper.BookMapper
import pet.project.app.model.domain.DomainBook
import pet.project.app.service.BookService
import java.math.BigDecimal
import java.time.Year

@WebMvcTest(BookController::class)
@Import(BookMapper::class)
class BookControllerValidationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var bookServiceMock: BookService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `should return bad request when creating book with empty title`() {
        // GIVEN
        val request = CreateBookRequest("", "Description", 2020, BigDecimal(20.99), 10)

        // WHEN
        val result = mockMvc.perform(
            post("/book/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andReturn()

        // THEN
        val response = objectMapper.readValue(result.response.contentAsString, ValidationExceptionResponse::class.java)
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status)
        assertEquals(1, response.invalidInputReports.size)
        assertEquals("title", response.invalidInputReports[0].field)
        assertEquals("Book title should not be empty", response.invalidInputReports[0].message)
        verify(exactly = 0) { bookServiceMock.create(any()) }
    }

    @ParameterizedTest
    @MethodSource("invalidYears")
    fun `should return bad request when creating book with invalid year`(invalidYear: Int) {
        // GIVEN
        val request = CreateBookRequest("Title", "Description", invalidYear, BigDecimal(20.99), 10)

        // WHEN
        val result = mockMvc.perform(
            post("/book/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andReturn()

        // THEN
        val response = objectMapper.readValue(result.response.contentAsString, ValidationExceptionResponse::class.java)
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status)
        assertEquals(1, response.invalidInputReports.size)
        assertEquals("yearOfPublishing", response.invalidInputReports[0].field)
        val actualMessage = response.invalidInputReports[0].message
        assertEquals("Publishing year of the book should be within a valid range", actualMessage)
        verify(exactly = 0) { bookServiceMock.create(any()) }
    }

    companion object {
        @Value("\${validation.params.future-years-allowance:5}")

        @JvmStatic
        fun invalidYears() = listOf(
            Arguments.of(1599),
            Arguments.of(Year.now().value + 6)
        )
    }

    @Test
    fun `should return bad request when creating book with negative price`() {
        // GIVEN
        val request = CreateBookRequest("Title", "Description", 2020, BigDecimal(-20.99), 10)

        // WHEN
        val result = mockMvc.perform(
            post("/book/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andReturn()

        // THEN
        val response = objectMapper.readValue(result.response.contentAsString, ValidationExceptionResponse::class.java)
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status)
        assertEquals(1, response.invalidInputReports.size)
        assertEquals("price", response.invalidInputReports[0].field)
        assertEquals("Book price should be greater than zero", response.invalidInputReports[0].message)
        verify(exactly = 0) { bookServiceMock.create(any()) }
    }

    @Test
    fun `should return bad request when creating book with negative amountAvailable`() {
        // GIVEN
        val request = CreateBookRequest("Title", "Description", 2020, BigDecimal(20.99), -10)

        // WHEN
        val result = mockMvc.perform(
            post("/book/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andReturn()

        // THEN
        val response = objectMapper.readValue(result.response.contentAsString, ValidationExceptionResponse::class.java)
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status)
        assertEquals(1, response.invalidInputReports.size)
        assertEquals("amountAvailable", response.invalidInputReports[0].field)
        assertEquals("Book amount cannot be negative", response.invalidInputReports[0].message)
        verify(exactly = 0) { bookServiceMock.create(any()) }
    }

    @Test
    fun `should return bad request when updating book with invalid ObjectId`() {
        // GIVEN
        val bookId = "InvalidObjectId"
        val request = UpdateBookRequest( "Title", "Description", 2020, BigDecimal(20.99))

        // WHEN
        val result = mockMvc.perform(
            put("/book/{id}", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andReturn()

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.response.status)
        val exception = result.resolvedException as HandlerMethodValidationException
        val actualMessage = exception.detailMessageArguments[0]
        assertEquals("The provided ID must be a valid ObjectId hex String", actualMessage)
        verify(exactly = 0) { bookServiceMock.update(any(), any()) }
    }

    @Test
    fun `should update book successfully when yearOfPublishing is null`() {
        // GIVEN
        val bookId = "66bf6bf8039339103054e21a"
        val request = UpdateBookRequest( "Title", "Updated", null, BigDecimal(20.99))
        val updated = DomainBook(bookId, "Title", "Updated", 2020, BigDecimal(20.99), 10)
        every { bookServiceMock.update(bookId, request) } returns updated

        // WHEN
        mockMvc.perform(
            put("/book/{id}", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isOk)

        // THEN
        verify { bookServiceMock.update(bookId, request) }
    }

    @Test
    fun `should return bad request when updating book with zero delta`() {
        // GIVEN
        val request = UpdateAmountRequest("66bf6bf8039339103054e21a", 0)

        // WHEN
        val result = mockMvc.perform(
            patch("/book/amount")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andReturn()

        // THEN
        val response = objectMapper.readValue(result.response.contentAsString, ValidationExceptionResponse::class.java)
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status)
        assertEquals(1, response.invalidInputReports.size)
        assertEquals("delta", response.invalidInputReports[0].field)
        assertEquals("Delta value must not be zero", response.invalidInputReports[0].message)
        verify(exactly = 0) { bookServiceMock.updateAmount(any()) }
    }

    @Test
    fun `should return bad request when deleting book with invalid ObjectId`() {
        // GIVEN
        val invalidObjectId = "invalidObjectId"

        // WHEN
        val result = mockMvc.perform(
            delete("/book/{id}", invalidObjectId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn()

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.response.status)
        val exception = result.resolvedException as HandlerMethodValidationException
        val actualMessage = exception.detailMessageArguments[0]
        assertEquals("The provided ID must be a valid ObjectId hex String", actualMessage)
        verify(exactly = 0) { bookServiceMock.delete(any()) }
    }

    @Test
    fun `should return bad request when getting book with invalid ObjectId`() {
        // GIVEN
        val invalidObjectId = "invalidObjectId"

        // WHEN
        val result = mockMvc.perform(
            get("/book/{id}", invalidObjectId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn()

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.response.status)
        val exception = result.resolvedException as HandlerMethodValidationException
        val actualMessage = exception.detailMessageArguments[0]
        assertEquals("The provided ID must be a valid ObjectId hex String", actualMessage)
        verify(exactly = 0) { bookServiceMock.updateAmount(any()) }
    }
}
