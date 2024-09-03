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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import pet.project.app.exception.BookNotFoundException
import pet.project.app.model.Book
import pet.project.app.repository.BookRepository

@ExtendWith(MockKExtension::class)
class BookServiceImplTest {

    @MockK
    lateinit var bookRepositoryMock: BookRepository

    @InjectMockKs
    lateinit var bookService: BookServiceImpl

    @Test
    fun `check create book`() {
        //GIVEN
        val inputBook = Book(
            title = "Title",
            description = "Description",
            yearOfPublishing = 200,
            price = 9.99,
            amountAvailable = 1
        )
        val expected = Book(ObjectId("66c3637847ff4c2f0242073e"), "Title", "Description", 200, 9.99, 1)
        every { bookRepositoryMock.save(inputBook) } returns expected

        //WHEN
        val actual = bookService.create(inputBook)

        //THEN
        verify { bookRepositoryMock.save(inputBook) }
        assertEquals(expected, actual)
    }

    @Test
    fun `check getting book by id`() {
        //GIVEN
        val testRequestBookId = "66c3637847ff4c2f0242073e"
        val expected = Book(ObjectId("66c3637847ff4c2f0242073e"), "Title", "Description", 200, 9.99, 1)
        every { bookRepositoryMock.findByIdOrNull(testRequestBookId) } returns expected

        //WHEN
        val actual = bookService.getById(testRequestBookId)

        //THEN
        verify { bookRepositoryMock.findByIdOrNull(testRequestBookId) }
        assertEquals(expected, actual)
    }

    @Test
    fun `check getting book by id throws exception when not found`() {
        //GIVEN
        val bookId = ObjectId.get().toHexString()
        every { bookRepositoryMock.findByIdOrNull(bookId) } returns null

        // WHEN & THEN
        assertThrows(BookNotFoundException::class.java) {
            bookService.getById(bookId)
        }
        verify { bookRepositoryMock.findByIdOrNull(bookId) }
    }

    @Test
    fun `check updating book`() {
        //GIVEN
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
        //GIVEN
        val book = Book(ObjectId.get(), "Test Book", "Description", 2023, 20.0, 10)
        every { bookRepositoryMock.existsById(book.id!!.toHexString()) } returns false

        // WHEN & THEN
        assertThrows(BookNotFoundException::class.java) {
            bookService.update(book)
        }
        verify { bookRepositoryMock.existsById(book.id!!.toHexString()) }
    }

    @Test
    fun `check changing amount of book successfully`() {
        //GIVEN
        val bookId = ObjectId.get().toHexString()
        val book = Book(ObjectId(bookId), "Test Book", "Description", 2023, 20.0, 10)
        every { bookRepositoryMock.findByIdOrNull(bookId) } returns book
        every { bookRepositoryMock.save(any()) } returns book.copy(amountAvailable = 15)

        // WHEN
        val updatedAmount = bookService.changeAmount(bookId, 5)

        // THEN
        assertEquals(15, updatedAmount)
        verify { bookRepositoryMock.findByIdOrNull(bookId) }
        verify { bookRepositoryMock.save(any()) }
    }

    @Test
    fun `check changing amount throws exception when insufficient books`() {
        //GIVEN
        val bookId = ObjectId.get().toHexString()
        val book = Book(ObjectId(bookId), "Test Book", "Description", 2023, 20.0, 3)
        every { bookRepositoryMock.findByIdOrNull(bookId) } returns book

        // WHEN & THEN
        assertThrows(IllegalStateException::class.java) {
            bookService.changeAmount(bookId, -4)
        }
        verify { bookRepositoryMock.findByIdOrNull(bookId) }
    }

    @Test
    fun `check deleting book`() {
        //GIVEN
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
        //GIVEN
        val bookId = ObjectId.get().toHexString()
        every { bookRepositoryMock.existsById(bookId) } returns false

        // WHEN
        bookService.delete(bookId)

        // THEN
        verify { bookRepositoryMock.existsById(bookId) }
        verify(exactly = 0) { bookRepositoryMock.deleteById(bookId) }
    }
}
