package pet.project.app.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
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
import org.springframework.web.method.annotation.HandlerMethodValidationException
import pet.project.app.dto.book.BookMapper
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.dto.book.UpdateBookRequest
import pet.project.app.exception.handler.ValidationExceptionResponse
import pet.project.app.service.BookService

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
        val request = CreateBookRequest("", "Description", 2020, 19.99, 10)

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

    @Test
    fun `should return bad request when creating book with invalid year`() {
        //GIVEN
        val request = CreateBookRequest("Title", "Description", 1500, 19.99, 10)

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
        assertEquals("Publishing year of the book should be within a valid range", response.invalidInputReports[0].message)
        verify(exactly = 0) { bookServiceMock.create(any()) }
    }

    @Test
    fun `should return bad request when creating book with negative price`() {
        // GIVEN
        val request = CreateBookRequest("Title", "Description", 2020, -19.99, 10)

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
        val request = CreateBookRequest("Title", "Description", 2020, 19.99, -10)

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
        val request = UpdateBookRequest("invalidObjectId", "Title", "Description", 2020, 19.99, 10)

        // WHEN
        val result = mockMvc.perform(
            put("/book/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andReturn()

        // THEN
        val response = objectMapper.readValue(result.response.contentAsString, ValidationExceptionResponse::class.java)
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status)
        assertEquals(1, response.invalidInputReports.size)
        assertEquals("id", response.invalidInputReports[0].field)
        assertEquals("The provided ID must be a valid ObjectId hex String", response.invalidInputReports[0].message)
        verify(exactly = 0) { bookServiceMock.update(any()) }
    }

    @Test
    fun `should return bad request when updating book with empty title`() {
        // GIVEN
        val request = UpdateBookRequest("507f191e810c19729de860ea", "", "Description", 2020, 19.99, 10)

        // WHEN
        val result = mockMvc.perform(
            put("/book/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andReturn()

        // THEN
        val response = objectMapper.readValue(result.response.contentAsString, ValidationExceptionResponse::class.java)
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status)
        assertEquals(1, response.invalidInputReports.size)
        assertEquals("title", response.invalidInputReports[0].field)
        assertEquals("Book title must not be blank", response.invalidInputReports[0].message)
        verify(exactly = 0) { bookServiceMock.update(any()) }
    }

    @Test
    fun `should return bad request when updating book with zero delta`() {
        // GIVEN
        val request = UpdateAmountRequest(0)

        // WHEN
        val result = mockMvc.perform(
            patch("/book/{id}/amount", "507f191e810c19729de860ea")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andReturn()

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.response.status)
        val exception = result.resolvedException as HandlerMethodValidationException
        val actualMessage = exception.detailMessageArguments.get(0)
        assertEquals("delta: Delta value must not be zero", actualMessage)
        verify(exactly = 0) { bookServiceMock.changeAmount(any(), any()) }
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
        val actualMessage = exception.detailMessageArguments.get(0)
        assertEquals("The provided ID must be a valid ObjectId hex String", actualMessage)
        verify(exactly = 0) { bookServiceMock.delete(any()) }
    }

    @Test
    fun `should return bad request when getting book with invalid ObjectId`() {
        //GIVEN
        val invalidObjectId = "invalidObjectId"

        //WHEN
        val result = mockMvc.perform(
            get("/book/{id}", invalidObjectId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn()

        //THEN
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.response.status)
        val exception = result.resolvedException as HandlerMethodValidationException
        val actualMessage = exception.detailMessageArguments.get(0)
        assertEquals("The provided ID must be a valid ObjectId hex String", actualMessage)
        verify(exactly = 0) { bookServiceMock.changeAmount(any(), any()) }
    }
}
