package pet.project.app.service.impl

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import org.awaitility.Awaitility.await
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.dto.book.UpdateBookRequest
import pet.project.app.exception.BookNotFoundException
import pet.project.app.model.domain.DomainBook
import pet.project.app.repository.BookRepository
import pet.project.app.service.NotificationService
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

@ExtendWith(MockKExtension::class)
class BookServiceImplTest {
    @MockK
    lateinit var bookRepositoryMock: BookRepository

    @MockK
    lateinit var notificationServiceMock: NotificationService

    @InjectMockKs
    lateinit var bookService: BookServiceImpl

    private val exampleDomainBook = DomainBook(
        ObjectId.get().toHexString(), "Test Book", "Description", 2023, BigDecimal(20.99), 10
    )

    @Test
    fun `check create book`() {
        // GIVEN
        val createBookRequest = CreateBookRequest("Test Book", "Description", 2023, BigDecimal(20.99), 10)

        every { bookRepositoryMock.insert(createBookRequest) } returns exampleDomainBook

        // WHEN
        val actual = bookService.create(createBookRequest)

        // THEN
        verify { bookRepositoryMock.insert(createBookRequest) }
        assertEquals(exampleDomainBook, actual)
    }

    @Test
    fun `check getting book by id`() {
        // GIVEN
        val testId = ObjectId.get().toHexString()
        val expected = exampleDomainBook.copy(id = testId)
        every { bookRepositoryMock.findById(testId) } returns expected

        // WHEN
        val actual = bookService.getById(testId)

        // THEN
        verify { bookRepositoryMock.findById(testId) }
        assertEquals(expected, actual)
    }

    @Test
    fun `check getting book by id throws exception when not found`() {
        // GIVEN
        val bookId = ObjectId.get().toHexString()
        every { bookRepositoryMock.findById(bookId) } returns null

        // WHEN & THEN
        assertThrows(BookNotFoundException::class.java) {
            bookService.getById(bookId)
        }
        verify { bookRepositoryMock.findById(bookId) }
    }

    @Test
    fun `check updating book successfully`() {
        // GIVEN
        val bookId = "66bf6bf8039339103054e21a"
        val updateBookRequest = UpdateBookRequest("Title", "Description", 2023, BigDecimal(20.0))
        val updatedDomainBook = DomainBook(bookId, "Title", "Description", 2023, BigDecimal(20.0), 10)
        every { bookRepositoryMock.update(bookId, updateBookRequest) } returns updatedDomainBook

        // WHEN
        val result = bookService.update(bookId, updateBookRequest)

        // THEN
        assertEquals(updatedDomainBook, result)
        verify { bookRepositoryMock.update(bookId, updateBookRequest) }
    }

    @Test
    fun `check updating book throws exception when attempting update absent book`() {
        // GIVEN
        every { bookRepositoryMock.update(any(), any()) } returns null
        val notExistingObjectId = ObjectId.get().toHexString()
        val updateBookRequest = UpdateBookRequest("Title", "Description", 2023, BigDecimal(20.0))

        // WHEN & THEN
        assertThrows(BookNotFoundException::class.java) {
            bookService.update(notExistingObjectId, updateBookRequest)
        }
        verify { bookRepositoryMock.update(any(), any()) }
    }

    @Test
    fun `check updating amount successfully`() {
        // GIVEN
        val bookId = ObjectId.get().toHexString()
        val testRequest = UpdateAmountRequest(bookId, 1)
        every { bookRepositoryMock.updateAmount(testRequest) } returns true
        every { notificationServiceMock.notifySubscribedUsers(bookId) } just Runs

        // WHEN
        val result = bookService.updateAmount(testRequest)

        // THEN
        assertTrue(result, "updateAmount() Should return true if operation succesfull")
        verify { bookRepositoryMock.updateAmount(testRequest) }
        await().atMost(3, TimeUnit.SECONDS).untilAsserted {
            verify { notificationServiceMock.notifySubscribedUsers(bookId) }
        }
    }

    @Test
    fun `check exchangeBooks and send books with increased amount succsessfully`() {
        // GIVEN
        val negativeDeltaRequest = UpdateAmountRequest(ObjectId.get().toHexString(), -1)
        val positiveDeltaRequest = UpdateAmountRequest(ObjectId.get().toHexString(), 1)
        val alsoNegativeDeltaRequest = UpdateAmountRequest(ObjectId.get().toHexString(), -3)
        val alsoPositiveDeltaRequest = UpdateAmountRequest(ObjectId.get().toHexString(), 3)
        val testRequests =
            listOf(negativeDeltaRequest, positiveDeltaRequest, alsoNegativeDeltaRequest, alsoPositiveDeltaRequest)
        every { bookRepositoryMock.updateAmountMany(testRequests) } returns testRequests.size
        val expectedList = listOf(positiveDeltaRequest.bookId, alsoPositiveDeltaRequest.bookId)
        every { notificationServiceMock.notifySubscribedUsers(expectedList) } just Runs

        // WHEN
        val result = bookService.exchangeBooks(testRequests)

        // THEN
        assertTrue(result, "exchangeBooks() Should return true if operation succesfull")
        verify { bookRepositoryMock.updateAmountMany(testRequests) }
        await().atMost(3, TimeUnit.SECONDS).untilAsserted {
            verify { notificationServiceMock.notifySubscribedUsers(expectedList) }
        }
    }

    @Test
    fun `should throw IllegalArgumentException when matched count is less than requests size`() {
        // GIVEN
        val requests = listOf(
            UpdateAmountRequest(ObjectId.get().toHexString(), 3),
            UpdateAmountRequest(ObjectId.get().toHexString(), 2)
        )
        val invalidMatchedCount = requests.size - 1
        every { bookRepositoryMock.updateAmountMany(requests) } returns invalidMatchedCount

        // WHEN & THEN
        val e = assertThrows(IllegalArgumentException::class.java) {
            bookService.exchangeBooks(requests)
        }
        assertEquals("Requested books absent or no enough available: $requests", e.message)
        verify { bookRepositoryMock.updateAmountMany(requests) }
    }

    @Test
    fun `check update amount throws exception when insufficient books`() {
        // GIVEN
        val bookId = ObjectId.get().toHexString()
        val request = UpdateAmountRequest(bookId, -4)
        every { bookRepositoryMock.updateAmount(request) } returns false

        // WHEN & THEN
        val e = assertThrows(IllegalArgumentException::class.java) {
            bookService.updateAmount(request)
        }
        assertEquals("Requested book absent or have less amount available that needed: $request", e.message)
        verify { bookRepositoryMock.updateAmount(request) }
    }

    @Test
    fun `check deleting book`() {
        // GIVEN
        val bookId = ObjectId.get().toHexString()
        every { bookRepositoryMock.delete(bookId) } returns 1L

        // WHEN
        bookService.delete(bookId)

        // THEN
        verify { bookRepositoryMock.delete(bookId) }
    }

    @Test
    fun `check update logs warn when affected documents count is not 1`() {
        // GIVEN
        val logger: Logger = LoggerFactory.getLogger(BookServiceImpl::class.java) as Logger
        val listAppender = ListAppender<ILoggingEvent>().apply { start() }
        logger.addAppender(listAppender)

        val bookId = ObjectId.get().toHexString()
        every { bookRepositoryMock.delete(bookId) } returns 0L

        // WHEN
        bookService.delete(bookId)

        // THEN
        verify { bookRepositoryMock.delete(bookId) }
        val logs = listAppender.list
        val expectedMessage = "Affected 0 documents while trying to delete book with id=$bookId"
        assertEquals(expectedMessage, logs.first().formattedMessage)
        assertEquals(Level.WARN, logs.first().level)
    }
}
