package pet.project.app.repository

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.model.Book
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BookRepositoryTest : AbstractMongoTestContainer {

    @Autowired
    private lateinit var bookRepository: BookRepository

    private val firstUnsavedBook = Book(
        title = "Second Sample Book",
        description = "Sample Description 1",
        yearOfPublishing = 2022,
        price = BigDecimal("19.99"),
        amountAvailable = 10,
        shouldBeNotified = false
    )

    private val secondUnsavedBook = Book(
        title = "First Sample Book",
        description = "Sample Description 2",
        yearOfPublishing = 2022,
        price = BigDecimal("29.99"),
        amountAvailable = 5,
        shouldBeNotified = false
    )

    @Test
    fun `insert should save book and assign id`() {
        // WHEN
        val actual = bookRepository.insert(firstUnsavedBook)

        // THEN
        assertTrue(actual.id != null, "Id should not be null after insert!")
    }

    @Test
    fun `findByIdOrNull should return saved book`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstUnsavedBook)

        // WHEN
        val actual = bookRepository.findByIdOrNull(savedBook.id.toString())

        // THEN
        assertEquals(savedBook, actual)
    }

    @Test
    fun `existsById should return true if book exists`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstUnsavedBook)

        // WHEN
        val actual = bookRepository.existsById(savedBook.id.toString())

        // THEN
        assertTrue(actual, "Book should exist!")
    }

    @Test
    fun `existsById should return false if book does not exist`() {
        // WHEN
        val actual = bookRepository.existsById(ObjectId().toHexString())

        // THEN
        assertFalse(actual, "Book should not exist!")
    }

    @Test
    fun `findByIdOrNull should return null if book does not exist`() {
        // WHEN
        val actual = bookRepository.findByIdOrNull(ObjectId().toHexString())

        // THEN
        assertNull(actual, "Should return null when book does not exist!")
    }

    @Test
    fun `setShouldBeNotified should update the field shouldBeNotified`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstUnsavedBook)

        // WHEN
        val modifiedCount = bookRepository.setShouldBeNotified(savedBook.id.toString(), true)

        // THEN
        val updatedBook = bookRepository.findByIdOrNull(savedBook.id.toString())
        assertNotNull(updatedBook)
        assertEquals(1L, modifiedCount, "Modified count should be 1")
        assertTrue(updatedBook.shouldBeNotified, "shouldBeNotified should be true")
    }

    @Test
    fun `update should replace book document`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstUnsavedBook)
        val updatedBook = savedBook.copy(
            title = "Updated Title",
            description = "Updated Description",
            price = BigDecimal("25.99")
        )

        // WHEN
        val modifiedCount = bookRepository.update(updatedBook)

        // THEN
        val actualBook = bookRepository.findByIdOrNull(savedBook.id.toString())
        assertEquals(1L, modifiedCount, "Modified count should be 1")
        assertEquals(updatedBook, actualBook, "Book should be updated with new values")
    }


    @Test
    fun `updateAmount should increase amountAvailable when delta is positive`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstUnsavedBook)
        val request = UpdateAmountRequest(savedBook.id.toString(), 5)

        // WHEN
        val modifiedCount = bookRepository.updateAmount(request)

        // THEN
        val updatedBook = bookRepository.findByIdOrNull(savedBook.id.toString())
        assertNotNull(updatedBook)
        assertEquals(1, modifiedCount, "Modified count should be 1")
        val expected = savedBook.amountAvailable + 5
        assertEquals(expected, updatedBook.amountAvailable, "AmountAvailable should be increased by 5")
        assertFalse(updatedBook.shouldBeNotified, "shouldBeNotified should remain unchanged")
    }

    @Test
    fun `updateAmount should decrease amountAvailable when delta is negative and sufficient amountAvailable`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstUnsavedBook)
        val request = UpdateAmountRequest(savedBook.id.toString(), -3)

        // WHEN
        val modifiedCount = bookRepository.updateAmount(request)

        // THEN
        val updatedBook = bookRepository.findByIdOrNull(savedBook.id.toString())
        assertNotNull(updatedBook)
        assertEquals(1, modifiedCount, "Modified count should be 1")
        assertEquals(7, updatedBook.amountAvailable, "AmountAvailable should be decreased by 3")
        assertFalse(updatedBook.shouldBeNotified, "shouldBeNotified should remain unchanged")
    }

    @Test
    fun `updateAmount should not modify amountAvailable when delta is negative and insufficient amountAvailable`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstUnsavedBook)
        val request = UpdateAmountRequest(savedBook.id.toString(), -11)

        // WHEN
        val modifiedCount = bookRepository.updateAmount(request)

        // THEN
        val updatedBook = bookRepository.findByIdOrNull(savedBook.id.toString())
        assertNotNull(updatedBook)
        assertEquals(0, modifiedCount, "Modified count should be 0 as update should not happen")
        assertEquals(10, updatedBook.amountAvailable, "AmountAvailable should remain unchanged")
        assertFalse(updatedBook.shouldBeNotified, "shouldBeNotified should remain unchanged")
    }

    @Test
    fun `updateAmount should set shouldBeNotified to true when amountAvailable equals delta`() {
        // GIVEN
        val bookWithZeroAmount = bookRepository.insert(firstUnsavedBook.copy(amountAvailable = 0))
        val request = UpdateAmountRequest(bookWithZeroAmount.id.toString(), 3)

        // WHEN
        val modifiedCount = bookRepository.updateAmount(request)

        // THEN
        val updatedBook = bookRepository.findByIdOrNull(bookWithZeroAmount.id.toString())
        assertNotNull(updatedBook)
        assertEquals(1, modifiedCount, "Modified count should be 1")
        assertEquals(3, updatedBook.amountAvailable, "AmountAvailable should be updated to 3")
        assertTrue(
            updatedBook.shouldBeNotified,
            "shouldBeNotified should be set to true when amountAvailable equals delta"
        )
    }

    @Test
    fun `updateAmountMany should update multiple books successfully`() {
        // GIVEN
        val firstSavedBook = bookRepository.insert(firstUnsavedBook)
        val secondSavedBook = bookRepository.insert(secondUnsavedBook)
        val requests = listOf(
            UpdateAmountRequest(firstSavedBook.id.toString(), 5),
            UpdateAmountRequest(secondSavedBook.id.toString(), -3)
        )

        // WHEN
        val matchedCount = bookRepository.updateAmountMany(requests)

        // THEN
        val firstUpdatedBook = bookRepository.findByIdOrNull(firstSavedBook.id.toString())
        assertNotNull(firstUpdatedBook)
        val secondUpdatedBook = bookRepository.findByIdOrNull(secondSavedBook.id.toString())
        assertNotNull(secondUpdatedBook)
        assertEquals(2, matchedCount, "Matched count should be equal to requests size")
        assertEquals(15, firstUpdatedBook.amountAvailable, "Book 1 amountAvailable should be updated correctly")
        assertEquals(2, secondUpdatedBook.amountAvailable, "Book 2 amountAvailable should be updated correctly")
    }

    @Test
    fun `updateAmountMany should abort transaction when update fails`() {
        // GIVEN
        val firstSavedBook = bookRepository.insert(firstUnsavedBook)
        val secondSavedBook = bookRepository.insert(secondUnsavedBook)

        val requests = listOf(
            UpdateAmountRequest(firstSavedBook.id.toString(), 5),
            UpdateAmountRequest(secondSavedBook.id.toString(), -10)  // This will fail
        )

        // WHEN
        val matchedCount = bookRepository.updateAmountMany(requests)

        // THEN
        val firstUpdatedBook = bookRepository.findByIdOrNull(firstSavedBook.id.toString())
        assertNotNull(firstUpdatedBook)
        val secondUpdatedBook = bookRepository.findByIdOrNull(secondSavedBook.id.toString())
        assertNotNull(secondUpdatedBook)
        assertEquals(1, matchedCount, "Only first book should match query")
        assertEquals(
            firstUnsavedBook.amountAvailable, firstUpdatedBook.amountAvailable,
            "Book 1 should not be updated due to transaction rollback"
        )
        assertEquals(
            secondUnsavedBook.amountAvailable, secondUpdatedBook.amountAvailable,
            "Book 2 should not be updated due to transaction rollback"
        )
    }

    @Test
    fun `updateAmountMany should handle case where delta is negative but less than available amount`() {
        // GIVEN
        val firstSavedBook = bookRepository.insert(firstUnsavedBook)
        val secondSavedBook = bookRepository.insert(secondUnsavedBook)
        val requests = listOf(
            UpdateAmountRequest(firstSavedBook.id.toString(), -2),  // Valid request
            UpdateAmountRequest(secondSavedBook.id.toString(), 3)   // Valid request
        )

        // WHEN
        val matchedCount = bookRepository.updateAmountMany(requests)

        // THEN
        val firstUpdatedBook = bookRepository.findByIdOrNull(firstSavedBook.id.toString())
        assertNotNull(firstUpdatedBook)
        val secondUpdatedBook = bookRepository.findByIdOrNull(secondSavedBook.id.toString())
        assertNotNull(secondUpdatedBook)
        assertEquals(2, matchedCount, "Matched count should be equal to requests size")
        assertEquals(8, firstUpdatedBook.amountAvailable, "Book 1 amountAvailable should be decreased correctly")
        assertEquals(8, secondUpdatedBook.amountAvailable, "Book 2 amountAvailable should be increased correctly")
    }

    @Test
    fun `delete should remove book by id`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstUnsavedBook)

        // WHEN
        val deletedCount = bookRepository.delete(savedBook.id.toString())

        // THEN
        val actualBook = bookRepository.findByIdOrNull(savedBook.id.toString())
        assertEquals(1L, deletedCount, "Deleted count should be 1")
        assertNull(actualBook, "Book should be deleted from the database")
    }
}
