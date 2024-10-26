package pet.project.app.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.book.ResponseBookDto
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.dto.book.UpdateBookRequest
import pet.project.app.model.domain.DomainBook
import pet.project.app.service.BookService
import pet.project.core.RandomTestFields.Book.amountAvailable
import pet.project.core.RandomTestFields.Book.bookId
import pet.project.core.RandomTestFields.Book.bookIdString
import pet.project.core.RandomTestFields.Book.description
import pet.project.core.RandomTestFields.Book.price
import pet.project.core.RandomTestFields.Book.title
import pet.project.core.RandomTestFields.Book.yearOfPublishing
import reactor.kotlin.core.publisher.toMono

@WebFluxTest(BookController::class)
class BookControllerTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var bookService: BookService

    @Test
    fun `should return book details when book id is valid`() {
        // GIVEN
        val initializedBook = DomainBook(bookIdString, title, description, yearOfPublishing, price, amountAvailable)
        val expected = ResponseBookDto(bookIdString, title, description, yearOfPublishing, price, amountAvailable)
        every { bookService.getById(bookIdString) } returns initializedBook.toMono()

        // WHEN & THEN
        webTestClient.get()
            .uri("/book/{id}", bookId)
            .exchange()
            .expectStatus().isOk
            .expectBody<ResponseBookDto>()
            .isEqualTo(expected)

        verify { bookService.getById(bookIdString) }
    }

    @Test
    fun `should create book successfully when request is valid`() {
        // GIVEN
        val createBookRequest = CreateBookRequest(title, description, yearOfPublishing, price, amountAvailable)
        val initializedBook = DomainBook(bookIdString, title, description, yearOfPublishing, price, amountAvailable)
        val expected = ResponseBookDto(bookIdString, title, description, yearOfPublishing, price, amountAvailable)
        every { bookService.create(createBookRequest) } returns initializedBook.toMono()

        // WHEN & THEN
        webTestClient.post()
            .uri("/book")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(createBookRequest)
            .exchange()
            .expectStatus().isCreated
            .expectBody<ResponseBookDto>()
            .isEqualTo(expected)

        verify { bookService.create(createBookRequest) }
    }

    @Test
    fun `should update book details when request is valid`() {
        // GIVEN
        val updateBookRequest = UpdateBookRequest(title, description, yearOfPublishing, price)
        val updatedDomainBook = DomainBook(bookIdString, title, description, yearOfPublishing, price, amountAvailable)
        val expected = ResponseBookDto(bookIdString, title, description, yearOfPublishing, price, amountAvailable)
        every { bookService.update(bookIdString, updateBookRequest) } returns updatedDomainBook.toMono()

        // WHEN & THEN
        webTestClient.put()
            .uri("/book/{id}", bookId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updateBookRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody<ResponseBookDto>()
            .isEqualTo(expected)

        verify { bookService.update(bookIdString, updateBookRequest) }
    }

    @Test
    fun `should update book amount when request is valid`() {
        // GIVEN
        val updateAmountRequest = UpdateAmountRequest(bookIdString, 5)
        every { bookService.updateAmount(updateAmountRequest) } returns Unit.toMono()

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
        every { bookService.delete(bookIdString) } returns Unit.toMono()

        // WHEN & THEN
        webTestClient.delete()
            .uri("/book/{id}", bookId)
            .exchange()
            .expectStatus().isNoContent

        verify { bookService.delete(bookIdString) }
    }
}
