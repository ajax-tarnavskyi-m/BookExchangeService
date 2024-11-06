package pet.project.app.service.impl

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.dto.book.UpdateBookRequest
import pet.project.app.kafka.BookAmountIncreasedProducer
import pet.project.app.model.domain.DomainBook
import pet.project.app.repository.BookRepository
import pet.project.core.RandomTestFields.Book.randomAmountAvailable
import pet.project.core.RandomTestFields.Book.randomBookIdString
import pet.project.core.RandomTestFields.Book.randomDescription
import pet.project.core.RandomTestFields.Book.randomPrice
import pet.project.core.RandomTestFields.Book.randomTitle
import pet.project.core.RandomTestFields.Book.randomYearOfPublishing
import pet.project.core.exception.BookNotFoundException
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test

@ExtendWith(MockKExtension::class)
class BookServiceImplTest {
    @MockK
    lateinit var bookRepositoryMock: BookRepository

    @MockK
    lateinit var producer: BookAmountIncreasedProducer

    @InjectMockKs
    lateinit var bookService: BookServiceImpl

    private val exampleBook = DomainBook(
        randomBookIdString(),
        randomTitle(),
        randomDescription(),
        randomYearOfPublishing(),
        randomPrice(),
        randomAmountAvailable()
    )

    @Test
    fun `should create book successfully`() {
        // GIVEN
        val createBookRequest = CreateBookRequest(
            randomTitle(),
            randomDescription(),
            randomYearOfPublishing(),
            randomPrice(),
            randomAmountAvailable()
        )

        every { bookRepositoryMock.insert(createBookRequest) } returns exampleBook.toMono()

        // WHEN
        val actualMono = bookService.create(createBookRequest)

        // THEN
        actualMono.test()
            .expectNext(exampleBook)
            .verifyComplete()

        verify { bookRepositoryMock.insert(createBookRequest) }
    }

    @Test
    fun `should get book by id successfully`() {
        // GIVEN
        val bookIdString = randomBookIdString()
        every { bookRepositoryMock.findById(bookIdString) } returns exampleBook.toMono()

        // WHEN
        val actualMono = bookService.getById(bookIdString)

        // THEN
        actualMono.test()
            .expectNext(exampleBook)
            .verifyComplete()

        verify { bookRepositoryMock.findById(bookIdString) }
    }

    @Test
    fun `should throw exception when book not found by id`() {
        // GIVEN
        val nonExisingId = randomBookIdString()
        every { bookRepositoryMock.findById(nonExisingId) } returns Mono.empty()

        // WHEN
        val actualMono = bookService.getById(nonExisingId)

        // THEN
        actualMono.test()
            .consumeErrorWith { ex ->
                assertEquals(BookNotFoundException::class.java, ex.javaClass)
                assertEquals("Could not find book $nonExisingId during GET request", ex.message)
            }.verify()

        verify { bookRepositoryMock.findById(nonExisingId) }
    }

    @Test
    fun `should update book successfully`() {
        // GIVEN
        val bookIdString = randomBookIdString()
        val request = UpdateBookRequest(randomTitle(), randomDescription(), randomYearOfPublishing(), randomPrice())
        val updatedBook = DomainBook(
            bookIdString,
            request.title!!,
            request.description!!,
            request.yearOfPublishing!!,
            request.price!!,
            randomAmountAvailable()
        )
        every { bookRepositoryMock.update(bookIdString, request) } returns updatedBook.toMono()

        // WHEN
        val actualMono = bookService.update(bookIdString, request)

        // THEN
        actualMono.test()
            .expectNext(updatedBook)
            .verifyComplete()

        verify { bookRepositoryMock.update(bookIdString, request) }
    }

    @Test
    fun `should throw exception when updating non-existent book`() {
        // GIVEN
        every { bookRepositoryMock.update(any(), any()) } returns Mono.empty()
        val notExistingIdString = randomBookIdString()
        val updateBookRequest =
            UpdateBookRequest(randomTitle(), randomDescription(), randomYearOfPublishing(), randomPrice())

        // WHEN
        val actualMono = bookService.update(notExistingIdString, updateBookRequest)

        // THEN
        actualMono.test()
            .verifyError(BookNotFoundException::class.java)

        verify { bookRepositoryMock.update(any(), any()) }
    }

    @Test
    fun `should update amount and notify subscribed users`() {
        // GIVEN
        val bookIdString = randomBookIdString()
        val testRequest = UpdateAmountRequest(bookIdString, 1)
        every { bookRepositoryMock.updateAmount(testRequest) } returns true.toMono()
        every { producer.sendMessages(listOf(bookIdString)) } returns Unit

        // WHEN
        val actualMono = bookService.updateAmount(testRequest)

        // THEN
        actualMono.test()
            .expectNext(Unit)
            .verifyComplete()

        verify { bookRepositoryMock.updateAmount(testRequest) }
        verify { producer.sendMessages(listOf(bookIdString)) }
    }

    @Test
    fun `should exchange books and notify successfully when amount increased`() {
        // GIVEN
        val negativeDeltaRequest = UpdateAmountRequest(randomBookIdString(), -1)
        val positiveDeltaRequest = UpdateAmountRequest(randomBookIdString(), 1)
        val alsoNegativeDeltaRequest = UpdateAmountRequest(randomBookIdString(), -3)
        val alsoPositiveDeltaRequest = UpdateAmountRequest(randomBookIdString(), 3)
        val testRequests =
            listOf(negativeDeltaRequest, positiveDeltaRequest, alsoNegativeDeltaRequest, alsoPositiveDeltaRequest)
        every { bookRepositoryMock.updateAmountMany(testRequests) } returns testRequests.size.toMono()
        val positiveDeltas = listOf(positiveDeltaRequest.bookId, alsoPositiveDeltaRequest.bookId)
        every { producer.sendMessages(positiveDeltas) } returns Unit

        // WHEN
        val actualMono = bookService.exchangeBooks(testRequests)

        // THEN
        actualMono.test()
            .expectNext(Unit)
            .verifyComplete()

        verify { bookRepositoryMock.updateAmountMany(testRequests) }
        verify { producer.sendMessages(positiveDeltas) }
    }

    @Test
    fun `should throw exception when insufficient books for update`() {
        // GIVEN
        val request = UpdateAmountRequest(randomBookIdString(), -4)
        every { bookRepositoryMock.updateAmount(request) } returns false.toMono()

        // WHEN
        val actualAmount = bookService.updateAmount(request)

        // THEN
        actualAmount.test()
            .consumeErrorWith { ex ->
                assertEquals(IllegalArgumentException::class.java, ex.javaClass)
                assertEquals("Book absent or no enough available: $request", ex.message)
            }.verify()

        verify { bookRepositoryMock.updateAmount(request) }
    }

    @Test
    fun `should delete book successfully`() {
        // GIVEN
        val bookIdString = randomBookIdString()
        every { bookRepositoryMock.delete(bookIdString) } returns 1L.toMono()

        // WHEN
        val actualMono = bookService.delete(bookIdString)

        // THEN
        actualMono.test()
            .expectNext(Unit)
            .verifyComplete()

        verify { bookRepositoryMock.delete(bookIdString) }
    }

    @Test
    fun `should log warning when affected documents count is not 1 during delete`() {
        // GIVEN
        val logger: Logger = LoggerFactory.getLogger(BookServiceImpl::class.java) as Logger
        val listAppender = ListAppender<ILoggingEvent>().apply { start() }
        logger.addAppender(listAppender)

        val bookIdString = randomBookIdString()
        every { bookRepositoryMock.delete(bookIdString) } returns 0L.toMono()

        // WHEN
        val actualMono = bookService.delete(bookIdString)

        // THEN
        actualMono.test()
            .expectNext(Unit)
            .verifyComplete()

        verify { bookRepositoryMock.delete(bookIdString) }
        val logs = listAppender.list
        val expectedMessage = "Affected 0 documents while trying to delete book with id=$bookIdString"
        assertEquals(expectedMessage, logs.first().formattedMessage)
        assertEquals(Level.WARN, logs.first().level)
    }
}
