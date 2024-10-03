package pet.project.app.service.impl

import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.exception.BookNotFoundException
import pet.project.app.model.Book
import pet.project.app.repository.BookRepository
import pet.project.app.service.NotificationService
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
class BookServiceImplTest {
    @MockK
    lateinit var bookRepositoryMock: BookRepository

    @MockK
    lateinit var notificationServiceMock: NotificationService

    @InjectMockKs
    lateinit var bookService: BookServiceImpl

    @Test
    fun `check create book`() {
        // GIVEN
        val inputBook = Book(null, "Title", "Description", 200, BigDecimal(20.99), 1)
        val expected = Book(ObjectId("66c3637847ff4c2f0242073e"), "Title", "Description", 200, BigDecimal(20.99), 1)
        every { bookRepositoryMock.insert(inputBook) } returns expected

        // WHEN
        val actual = bookService.create(inputBook)

        // THEN
        verify { bookRepositoryMock.insert(inputBook) }
        assertEquals(expected, actual)
    }

    @Test
    fun `check getting book by id`() {
        // GIVEN
        val testRequestBookId = "66c3637847ff4c2f0242073e"
        val expected = Book(ObjectId("66c3637847ff4c2f0242073e"), "Title", "Description", 200, BigDecimal(20.99), 1)
        every { bookRepositoryMock.findByIdOrNull(testRequestBookId) } returns expected

        // WHEN
        val actual = bookService.getById(testRequestBookId)

        // THEN
        verify { bookRepositoryMock.findByIdOrNull(testRequestBookId) }
        assertEquals(expected, actual)
    }

    @Test
    fun `check getting book by id throws exception when not found`() {
        // GIVEN
        val bookId = ObjectId.get().toHexString()
        every { bookRepositoryMock.findByIdOrNull(bookId) } returns null

        // WHEN & THEN
        assertThrows(BookNotFoundException::class.java) {
            bookService.getById(bookId)
        }
        verify { bookRepositoryMock.findByIdOrNull(bookId) }
    }

    @Test
    fun `check updating book successfully`() {
        // GIVEN
        val book = Book(ObjectId.get(), "Test Book", "Description", 2023, BigDecimal(20.99), 10)
        every { bookRepositoryMock.update(book) } returns 1L

        // WHEN
        val result = bookService.update(book)

        // THEN
        assertEquals(book, result)
        verify { bookRepositoryMock.update(book) }
    }

    @Test
    fun `check updating book throws exception when match count is 0`() {
        // GIVEN
        val book = Book(ObjectId.get(), "Test Book", "Description", 2023, BigDecimal(20.99), 10)
        every { bookRepositoryMock.update(book) } returns 0

        // WHEN & THEN
        assertThrows(BookNotFoundException::class.java) {
            bookService.update(book)
        }
        verify { bookRepositoryMock.update(book) }
    }

    @Test
    fun `check updating amount successfully`() {
        // GIVEN
        val bookId = ObjectId.get().toHexString()
        val testRequest = UpdateAmountRequest(bookId, 1)
        every { bookRepositoryMock.updateAmount(testRequest) } returns 1L
        every { notificationServiceMock.notifySubscribedUsers(bookId) } just Runs

        // WHEN
        val result = bookService.updateAmount(testRequest)

        // THEN
        assertTrue(result)
        verify { bookRepositoryMock.updateAmount(testRequest) }
        verify { notificationServiceMock.notifySubscribedUsers(bookId) }
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
        every {
            notificationServiceMock.notifySubscribedUsers(
                listOf(positiveDeltaRequest.bookId, alsoPositiveDeltaRequest.bookId)
            )
        } just Runs

        // WHEN
        val result = bookService.exchangeBooks(testRequests)

        // THEN
        assertTrue(result)
        verify { bookRepositoryMock.updateAmountMany(testRequests) }
        verify {
            notificationServiceMock.notifySubscribedUsers(
                listOf(positiveDeltaRequest.bookId, alsoPositiveDeltaRequest.bookId)
            )
        }
    }

    @Test
    fun `check exchangeBooks dont notify when requests have only negative delta`() {
        // GIVEN
        val negativeDeltaRequest = UpdateAmountRequest(ObjectId.get().toHexString(), -1)
        val alsoNegativeDeltaRequest = UpdateAmountRequest(ObjectId.get().toHexString(), -3)
        val testRequests = listOf(negativeDeltaRequest, alsoNegativeDeltaRequest)
        every { bookRepositoryMock.updateAmountMany(testRequests) } returns testRequests.size

        // WHEN
        val result = bookService.exchangeBooks(testRequests)

        assertTrue(result)
        verify { bookRepositoryMock.updateAmountMany(testRequests) }
        verify(exactly = 0) { notificationServiceMock.notifySubscribedUsers(any<List<String>>()) }
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
        assertEquals("Requested books absent or have less amount available that needed: $requests", e.message)
        verify { bookRepositoryMock.updateAmountMany(requests) }
    }

    @Test
    fun `check update amount throws exception when insufficient books`() {
        //GIVEN
        val bookId = ObjectId.get().toHexString()
        val request = UpdateAmountRequest(bookId, -4)
        every { bookRepositoryMock.updateAmount(request) } returns 0

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
}
