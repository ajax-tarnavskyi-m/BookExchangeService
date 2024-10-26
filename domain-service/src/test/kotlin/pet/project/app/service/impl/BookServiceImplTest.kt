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
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.dto.book.UpdateBookRequest
import pet.project.app.model.domain.DomainBook
import pet.project.app.repository.BookRepository
import pet.project.core.RandomTestFields.Book.amountAvailable
import pet.project.core.RandomTestFields.Book.bookIdString
import pet.project.core.RandomTestFields.Book.description
import pet.project.core.RandomTestFields.Book.price
import pet.project.core.RandomTestFields.Book.title
import pet.project.core.RandomTestFields.Book.yearOfPublishing
import pet.project.core.RandomTestFields.SecondBook.secondDescription
import pet.project.core.RandomTestFields.SecondBook.secondPrice
import pet.project.core.RandomTestFields.SecondBook.secondTitle
import pet.project.core.RandomTestFields.SecondBook.secondYearOfPublishing
import pet.project.core.exception.BookNotFoundException
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test

@ExtendWith(MockKExtension::class)
class BookServiceImplTest {
    @MockK
    lateinit var bookRepositoryMock: BookRepository

    @MockK
    lateinit var availableBooksSink: Sinks.Many<String>

    @InjectMockKs
    lateinit var bookService: BookServiceImpl

    private val exampleBook = DomainBook(bookIdString, title, description, yearOfPublishing, price, amountAvailable)

    @Test
    fun `should create book successfully`() {
        // GIVEN
        val createBookRequest = CreateBookRequest(title, description, yearOfPublishing, price, amountAvailable)

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
        val nonExisingId = ObjectId.get().toHexString()
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
        val updateBookRequest = UpdateBookRequest(secondTitle, secondDescription, secondYearOfPublishing, secondPrice)
        val updatedBook = DomainBook(bookIdString, secondTitle, description, yearOfPublishing, price, amountAvailable)
        every { bookRepositoryMock.update(bookIdString, updateBookRequest) } returns updatedBook.toMono()

        // WHEN
        val actualMono = bookService.update(bookIdString, updateBookRequest)

        // THEN
        actualMono.test()
            .expectNext(updatedBook)
            .verifyComplete()

        verify { bookRepositoryMock.update(bookIdString, updateBookRequest) }
    }

    @Test
    fun `should throw exception when updating non-existent book`() {
        // GIVEN
        every { bookRepositoryMock.update(any(), any()) } returns Mono.empty()
        val notExistingObjectId = ObjectId.get().toHexString()
        val updateBookRequest = UpdateBookRequest(secondTitle, secondDescription, secondYearOfPublishing, secondPrice)

        // WHEN
        val actualMono = bookService.update(notExistingObjectId, updateBookRequest)

        // THEN
        actualMono.test()
            .verifyError(BookNotFoundException::class.java)

        verify { bookRepositoryMock.update(any(), any()) }
    }

    @Test
    fun `should update amount and notify subscribed users`() {
        // GIVEN
        val bookId = ObjectId.get().toHexString()
        val testRequest = UpdateAmountRequest(bookId, 1)
        every { bookRepositoryMock.updateAmount(testRequest) } returns true.toMono()
        every { availableBooksSink.tryEmitNext(bookId) } returns Sinks.EmitResult.OK

        // WHEN
        val actualMono = bookService.updateAmount(testRequest)

        // THEN
        actualMono.test()
            .expectNext(Unit)
            .verifyComplete()

        verify { bookRepositoryMock.updateAmount(testRequest) }
        verify { availableBooksSink.tryEmitNext(bookId) }
    }

    @Test
    fun `should exchange books and notify successfully when amount increased`() {
        // GIVEN
        val negativeDeltaRequest = UpdateAmountRequest(ObjectId.get().toHexString(), -1)
        val positiveDeltaRequest = UpdateAmountRequest(ObjectId.get().toHexString(), 1)
        val alsoNegativeDeltaRequest = UpdateAmountRequest(ObjectId.get().toHexString(), -3)
        val alsoPositiveDeltaRequest = UpdateAmountRequest(ObjectId.get().toHexString(), 3)
        val testRequests =
            listOf(negativeDeltaRequest, positiveDeltaRequest, alsoNegativeDeltaRequest, alsoPositiveDeltaRequest)
        every { bookRepositoryMock.updateAmountMany(testRequests) } returns testRequests.size.toMono()
        every { availableBooksSink.tryEmitNext(positiveDeltaRequest.bookId) } returns Sinks.EmitResult.OK
        every { availableBooksSink.tryEmitNext(alsoPositiveDeltaRequest.bookId) } returns Sinks.EmitResult.OK

        // WHEN
        val actualMono = bookService.exchangeBooks(testRequests)

        // THEN
        actualMono.test()
            .expectNext(Unit)
            .verifyComplete()

        verify { bookRepositoryMock.updateAmountMany(testRequests) }
        val positiveDeltaRequestAmount = 2
        verify(exactly = positiveDeltaRequestAmount) { availableBooksSink.tryEmitNext(any()) }
    }

    @Test
    fun `should throw exception when insufficient books for update`() {
        // GIVEN
        val bookId = ObjectId.get().toHexString()
        val request = UpdateAmountRequest(bookId, -4)
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
        val bookId = ObjectId.get().toHexString()
        every { bookRepositoryMock.delete(bookId) } returns 1L.toMono()

        // WHEN
        val actualMono = bookService.delete(bookId)

        // THEN
        actualMono.test()
            .expectNext(Unit)
            .verifyComplete()

        verify { bookRepositoryMock.delete(bookId) }
    }

    @Test
    fun `should log warning when affected documents count is not 1 during delete`() {
        // GIVEN
        val logger: Logger = LoggerFactory.getLogger(BookServiceImpl::class.java) as Logger
        val listAppender = ListAppender<ILoggingEvent>().apply { start() }
        logger.addAppender(listAppender)

        val bookId = ObjectId.get().toHexString()
        every { bookRepositoryMock.delete(bookId) } returns 0L.toMono()

        // WHEN
        val actualMono = bookService.delete(bookId)

        // THEN
        actualMono.test()
            .expectNext(Unit)
            .verifyComplete()

        verify { bookRepositoryMock.delete(bookId) }
        val logs = listAppender.list
        val expectedMessage = "Affected 0 documents while trying to delete book with id=$bookId"
        assertEquals(expectedMessage, logs.first().formattedMessage)
        assertEquals(Level.WARN, logs.first().level)
    }
}
