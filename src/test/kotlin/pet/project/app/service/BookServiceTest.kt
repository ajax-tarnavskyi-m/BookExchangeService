package pet.project.app.service

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.exception.BookNotFoundException
import pet.project.app.model.Book
import pet.project.app.repository.BookRepository

class BookServiceTest {

    @MockK
    lateinit var bookRepositoryMock: BookRepository

    @InjectMockKs
    lateinit var bookService: BookService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `check create book`() {
        // GIVEN
        val inputBook = Book(
            title = "Title",
            description = "Description",
            yearOfPublishing = 200,
            price = 9.99,
            amountAvailable = 1
        )
        val expected = Book(ObjectId("66c3637847ff4c2f0242073e"), "Title", "Description", 200, 9.99, 1)
        every { bookRepositoryMock.save(inputBook) } returns expected

        // WHEN
        val actual = bookService.create(inputBook)

        // THEN
        verify { bookRepositoryMock.save(inputBook) }
        assertEquals(expected, actual)
    }

    @Test
    fun `check getting book by id`() {
        // GIVEN
        val testRequestBookId = "66c3637847ff4c2f0242073e"
        val expected = Book(ObjectId("66c3637847ff4c2f0242073e"), "Title", "Description", 200, 9.99, 1)
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

        // THEN
        assertThrows<BookNotFoundException> {
            // WHEN
            bookService.getById(bookId)
        }

        verify { bookRepositoryMock.findByIdOrNull(bookId) }
    }

    @Test
    fun `check updating book`() {
        // GIVEN
        val book = Book(ObjectId.get(), "Test Book", "Description", 2023, 20.0, 10)

        every { bookRepositoryMock.existsById(book.id!!.toHexString()) } returns true
        every { bookRepositoryMock.save(book) } returns book

        // WHEN
        val result = bookService.update(book)

        // THEN
        assertEquals(book, result)
        verify { bookRepositoryMock.existsById(book.id!!.toHexString()) }
        verify { bookRepositoryMock.save(book) }
    }

    @Test
    fun `check updating book throws exception when book not found`() {
        // GIVEN
        val book = Book(ObjectId("60f1b13e8f1b2c000b355777"), "Test Book", "Description", 2023, 20.0, 10)
        every { bookRepositoryMock.existsById("60f1b13e8f1b2c000b355777") } returns false

        // WHEN
        assertThrows<BookNotFoundException> {
            bookService.update(book)
        }

        // THEN
        verify { bookRepositoryMock.existsById("60f1b13e8f1b2c000b355777") }
    }


    @Test
    fun `changeAmount should update book amounts and return total changed amount`() {
        // GIVEN
        val book1 = Book(ObjectId("60f1b13e8f1b2c000b355777"), "Test Book", "Description", 2023, 20.0, 5)
        val book2 = Book(ObjectId("60f1b13e8f1b2c000b355778"), "Test Book", "Description", 2023, 20.0, 5)
        val updateRequests = listOf(
            UpdateAmountRequest("60f1b13e8f1b2c000b355777", 2),
            UpdateAmountRequest("60f1b13e8f1b2c000b355778", -1)
        )
        every { bookRepositoryMock.findAllById(listOf("60f1b13e8f1b2c000b355777", "60f1b13e8f1b2c000b355778")) } returns listOf(book1, book2)
        every { bookRepositoryMock.save(any()) } returns book1 andThen book2

        // WHEN
        val totalAmountChanged = bookService.changeAmount(updateRequests)

        // THEN
        assertEquals(3, totalAmountChanged) // 2 books added, 1 book removed
        verify(exactly = 2) { bookRepositoryMock.save(any()) }
    }

    @Test
    fun `changeAmount should throw if books not found`() {
        // GIVEN
        val updateRequests = listOf(
            UpdateAmountRequest(bookId = "60f1b13e8f1b2c000b355777", delta = 2)
        )

        every { bookRepositoryMock.findAllById(listOf("60f1b13e8f1b2c000b355777")) } returns emptyList()

        // WHEN
        val exception = assertThrows<RuntimeException> {
            bookService.changeAmount(updateRequests)
        }

        //THEN
        assertEquals("Could not find all books. Found [], while requested [60f1b13e8f1b2c000b355777]", exception.message)
    }

    @Test
    fun `changeAmount should throw if trying to withdraw more books than available`() {
        // GIVEN
        val book = Book(ObjectId("60f1b13e8f1b2c000b355777"), "Test Book", "Description", 2023, 20.0, 1)
        val updateRequest = UpdateAmountRequest("60f1b13e8f1b2c000b355777", -2)
        every { bookRepositoryMock.findAllById(listOf("60f1b13e8f1b2c000b355777")) } returns listOf(book)

        // WHEN
        val exception = assertThrows<IllegalStateException> {
            bookService.changeAmount(listOf(updateRequest))
        }

        //THEN
        assertEquals("Can't withdraw 2 book(s), when amount is 1", exception.message)
    }

    @Test
    fun `check deleting book`() {
        // GIVEN
        val bookId = ObjectId.get().toHexString()

        every { bookRepositoryMock.existsById(bookId) } returns true
        every { bookRepositoryMock.deleteById(bookId) } just Runs

        // WHEN
        bookService.delete(bookId)

        // THEN
        verify { bookRepositoryMock.existsById(bookId) }
        verify { bookRepositoryMock.deleteById(bookId) }
    }

    @Test
    fun `check deleting book when book does not exist`() {
        // GIVEN
        val bookId = ObjectId.get().toHexString()

        every { bookRepositoryMock.existsById(bookId) } returns false

        // WHEN
        bookService.delete(bookId)

        // THEN
        verify { bookRepositoryMock.existsById(bookId) }
        verify(exactly = 0) { bookRepositoryMock.deleteById(bookId) }
    }
}
