package pet.project.app.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.bson.types.ObjectId
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
import pet.project.core.RandomTestFields.Book.randomAmountAvailable
import pet.project.core.RandomTestFields.Book.randomBookIdString
import pet.project.core.RandomTestFields.Book.randomDescription
import pet.project.core.RandomTestFields.Book.randomPrice
import pet.project.core.RandomTestFields.Book.randomTitle
import pet.project.core.RandomTestFields.Book.randomYearOfPublishing
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
        val bookIdString = randomBookIdString()
        val domainBook = DomainBook(
            bookIdString,
            randomTitle(),
            randomDescription(),
            randomYearOfPublishing(),
            randomPrice(),
            randomAmountAvailable()
        )
        val expected = ResponseBookDto(
            bookIdString,
            domainBook.title,
            domainBook.description,
            domainBook.yearOfPublishing,
            domainBook.price,
            domainBook.amountAvailable
        )
        every { bookService.getById(bookIdString) } returns domainBook.toMono()

        // WHEN & THEN
        webTestClient.get()
            .uri("/book/{id}", ObjectId(bookIdString))
            .exchange()
            .expectStatus().isOk
            .expectBody<ResponseBookDto>()
            .isEqualTo(expected)

        verify { bookService.getById(bookIdString) }
    }

    @Test
    fun `should create book successfully when request is valid`() {
        // GIVEN
        val bookIdString = randomBookIdString()
        val request = CreateBookRequest(
            randomTitle(),
            randomDescription(),
            randomYearOfPublishing(),
            randomPrice(),
            randomAmountAvailable()
        )
        val initializedBook = DomainBook(
            bookIdString,
            request.title,
            request.description!!,
            request.yearOfPublishing,
            request.price,
            request.amountAvailable
        )
        val expected = ResponseBookDto(
            bookIdString,
            request.title,
            request.description!!,
            request.yearOfPublishing,
            request.price,
            request.amountAvailable
        )
        every { bookService.create(request) } returns initializedBook.toMono()

        // WHEN & THEN
        webTestClient.post()
            .uri("/book")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody<ResponseBookDto>()
            .isEqualTo(expected)

        verify { bookService.create(request) }
    }

    @Test
    fun `should update book details when request is valid`() {
        // GIVEN
        val bookIdString = randomBookIdString()
        val amountAvailable = randomAmountAvailable()
        val request = UpdateBookRequest(
            randomTitle(),
            randomDescription(),
            randomYearOfPublishing(),
            randomPrice()
        )
        val updatedDomainBook = DomainBook(
            bookIdString,
            request.title!!,
            request.description!!,
            request.yearOfPublishing!!,
            request.price!!,
            amountAvailable
        )
        val expected = ResponseBookDto(
            bookIdString,
            request.title!!,
            request.description!!,
            request.yearOfPublishing!!,
            request.price!!,
            amountAvailable
        )
        every { bookService.update(bookIdString, request) } returns updatedDomainBook.toMono()

        // WHEN & THEN
        webTestClient.put()
            .uri("/book/{id}", bookIdString)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody<ResponseBookDto>()
            .isEqualTo(expected)

        verify { bookService.update(bookIdString, request) }
    }

    @Test
    fun `should update book amount when request is valid`() {
        // GIVEN
        val bookIdString = randomBookIdString()
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
        val bookIdString = randomBookIdString()
        every { bookService.delete(bookIdString) } returns Unit.toMono()

        // WHEN & THEN
        webTestClient.delete()
            .uri("/book/{id}", ObjectId(bookIdString))
            .exchange()
            .expectStatus().isNoContent

        verify { bookService.delete(bookIdString) }
    }
}
