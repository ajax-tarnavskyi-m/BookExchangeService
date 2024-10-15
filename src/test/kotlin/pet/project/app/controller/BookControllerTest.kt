package pet.project.app.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.book.ResponseBookDto
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.dto.book.UpdateBookRequest
import pet.project.app.model.domain.DomainBook
import pet.project.app.service.BookService
import reactor.core.publisher.Mono
import java.math.BigDecimal

@WebFluxTest(BookController::class)
class BookControllerTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var bookService: BookService

    @Test
    fun `should return book details when book id is valid`() {
        // GIVEN
        val bookId = "66bf6bf8039339103054e21a"
        val initializedDomainBook = DomainBook(bookId, "Title", "Description", 2023, BigDecimal(20.0), 10)
        val expectedResponse = ResponseBookDto(bookId, "Title", "Description", 2023, BigDecimal(20.0), 10)
        every { bookService.getById(bookId) } returns Mono.just(initializedDomainBook)

        // WHEN & THEN
        webTestClient.get()
            .uri("/book/{id}", bookId)
            .exchange()
            .expectStatus().isOk
            .expectBody(ResponseBookDto::class.java)
            .isEqualTo(expectedResponse)

        verify { bookService.getById(bookId) }
    }

    @Test
    fun `should create book successfully when request is valid`() {
        // GIVEN
        val createBookRequest = CreateBookRequest("Title", "Description", 2023, BigDecimal(20.0), 10)
        val bookId = "66bf6bf8039339103054e21a"
        val initializedDomainBook = DomainBook(bookId, "Title", "Description", 2023, BigDecimal(20.0), 10)
        val expectedResponse = ResponseBookDto(bookId, "Title", "Description", 2023, BigDecimal(20.0), 10)
        every { bookService.create(createBookRequest) } returns Mono.just(initializedDomainBook)

        // WHEN & THEN
        webTestClient.post()
            .uri("/book/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(createBookRequest)
            .exchange()
            .expectStatus().isCreated
            .expectBody(ResponseBookDto::class.java)
            .isEqualTo(expectedResponse)

        verify { bookService.create(createBookRequest) }
    }

    @Test
    fun `should update book details when request is valid`() {
        // GIVEN
        val bookId = "66bf6bf8039339103054e21a"
        val updateBookRequest = UpdateBookRequest("Title", "Description", 2023, BigDecimal(20.0))
        val updatedDomainBook = DomainBook(bookId, "Title", "Description", 2023, BigDecimal(20.0), 10)
        val expectedResponse = ResponseBookDto(bookId, "Title", "Description", 2023, BigDecimal(20.0), 10)
        every { bookService.update(bookId, updateBookRequest) } returns Mono.just(updatedDomainBook)

        // WHEN & THEN
        webTestClient.put()
            .uri("/book/{id}", bookId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updateBookRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody(ResponseBookDto::class.java)
            .isEqualTo(expectedResponse)

        verify { bookService.update(bookId, updateBookRequest) }
    }

    @Test
    fun `should update book amount when request is valid`() {
        // GIVEN
        val bookId = "66bf6bf8039339103054e21a"
        val updateAmountRequest = UpdateAmountRequest(bookId, 5)
        every { bookService.updateAmount(updateAmountRequest) } returns Mono.just(Unit)

        // WHEN & THEN
        webTestClient.patch()
            .uri("/book/amount")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updateAmountRequest)
            .exchange()
            .expectStatus().isNoContent

        verify { bookService.updateAmount(updateAmountRequest) }
    }

    @Test
    fun `should delete book when book id is valid`() {
        // GIVEN
        val bookId = "66bf6bf8039339103054e21a"
        every { bookService.delete(bookId) } returns Mono.just(Unit)

        // WHEN & THEN
        webTestClient.delete()
            .uri("/book/{id}", bookId)
            .exchange()
            .expectStatus().isNoContent

        verify { bookService.delete(bookId) }
    }
}
