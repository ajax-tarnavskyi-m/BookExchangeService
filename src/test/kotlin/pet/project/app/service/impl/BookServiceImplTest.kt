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
import pet.project.app.exception.BookNotFoundException
import pet.project.app.model.domain.DomainBook
import pet.project.app.repository.BookRepository
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
class BookServiceImplTest {
    @MockK

    lateinit var bookRepositoryMock: BookRepository
    @MockK
    lateinit var availableBooksSink: Sinks.Many<String>

    @InjectMockKs
    lateinit var bookService: BookServiceImpl

    private val exampleDomainBook = DomainBook(
        ObjectId.get().toHexString(), "Test Book", "Description", 2023, BigDecimal(20.99), 10
    )

    @Test
    fun `should create book successfully`() {
        // GIVEN
        val createBookRequest = CreateBookRequest("Test Book", "Description", 2023, BigDecimal(20.99), 10)

        every { bookRepositoryMock.insert(createBookRequest) } returns exampleDomainBook.toMono()

        // WHEN
        val actualMono = bookService.create(createBookRequest)

        // THEN
        actualMono.test()
            .expectNext(exampleDomainBook)
            .verifyComplete()

        verify { bookRepositoryMock.insert(createBookRequest) }
    }


    @Test
    fun `should get book by id successfully`() {
        // GIVEN
        val testId = ObjectId.get().toHexString()
        val expected = exampleDomainBook.copy(id = testId)
        every { bookRepositoryMock.findById(testId) } returns expected.toMono()

        // WHEN
        val actualMono = bookService.getById(testId)

        // THEN
        actualMono.test()
            .expectNext(expected)
            .verifyComplete()

        verify { bookRepositoryMock.findById(testId) }
    }

    @Test
    fun `should throw exception when book not found by id`() {
        // GIVEN
        val bookId = ObjectId.get().toHexString()
        every { bookRepositoryMock.findById(bookId) } returns Mono.empty()

        // WHEN
        val actualMono = bookService.getById(bookId)

        // THEN
        actualMono.test()
            .consumeErrorWith { ex ->
                assertEquals(BookNotFoundException::class.java, ex.javaClass)
                assertEquals("Book with id=${bookId} was not found during GET request", ex.message)
            }.verify()

        verify { bookRepositoryMock.findById(bookId) }
    }

    @Test
    fun `should update book successfully`() {
        // GIVEN
        val bookId = "66bf6bf8039339103054e21a"
        val updateBookRequest = UpdateBookRequest("Title", "Description", 2023, BigDecimal(20.0))
        val updatedDomainBook = DomainBook(bookId, "Title", "Description", 2023, BigDecimal(20.0), 10)
        every { bookRepositoryMock.update(bookId, updateBookRequest) } returns updatedDomainBook.toMono()

        // WHEN
        val actualMono = bookService.update(bookId, updateBookRequest)

        // THEN
        actualMono.test()
            .expectNext(updatedDomainBook)
            .verifyComplete()

        verify { bookRepositoryMock.update(bookId, updateBookRequest) }
    }

    @Test
    fun `should throw exception when updating non-existent book`() {
        // GIVEN
        every { bookRepositoryMock.update(any(), any()) } returns Mono.empty()
        val notExistingObjectId = ObjectId.get().toHexString()
        val updateBookRequest = UpdateBookRequest("Title", "Description", 2023, BigDecimal(20.0))

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
