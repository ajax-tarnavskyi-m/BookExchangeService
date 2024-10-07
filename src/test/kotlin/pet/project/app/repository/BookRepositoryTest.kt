package pet.project.app.repository

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.book.UpdateAmountRequest
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BookRepositoryTest : AbstractMongoTestContainer {
    @Autowired
    private lateinit var bookRepository: BookRepository

    private val firstCreationRequest = CreateBookRequest(
        title = "Second Sample Book",
        description = "Sample Description 1",
        yearOfPublishing = 2022,
        price = BigDecimal("19.99"),
        amountAvailable = 10,
    )

    private val secondCreationRequest = CreateBookRequest(
        title = "First Sample Book",
        description = "Sample Description 2",
        yearOfPublishing = 2022,
        price = BigDecimal("29.99"),
        amountAvailable = 5,
    )

    @Test
    fun `insert should save book and assign valid id`() {
        // WHEN
        val actualBook = bookRepository.insert(firstCreationRequest)

        // THEN
        ObjectId.isValid(actualBook.id)
        assertEquals(firstCreationRequest.title, actualBook.title)
        assertEquals(firstCreationRequest.description, actualBook.description)
        assertEquals(firstCreationRequest.yearOfPublishing, actualBook.yearOfPublishing)
        assertEquals(firstCreationRequest.price, actualBook.price)
        assertEquals(firstCreationRequest.amountAvailable, actualBook.amountAvailable)
    }

    @Test
    fun `findByIdOrNull should return saved book`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstCreationRequest)

        // WHEN
        val actual = bookRepository.findById(savedBook.id.toString())

        // THEN
        assertEquals(savedBook, actual, "Saved book should be equals to retrieved one")
    }

    @Test
    fun `existsById should return true if book exists`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstCreationRequest)

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
        val actual = bookRepository.findById(ObjectId().toHexString())

        // THEN
        assertNull(actual, "Should return null when book does not exist!")
    }

    @Test
    fun `setShouldBeNotified should return modifiedCount 1 when updating field`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstCreationRequest)

        // WHEN
        val modifiedCount = bookRepository.updateShouldBeNotified(savedBook.id, true)

        // THEN
        assertEquals(1L, modifiedCount, "Modified count should be 1")
    }

    @Test
    fun `updateAmount should increase amountAvailable when delta is positive`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstCreationRequest)
        val request = UpdateAmountRequest(savedBook.id, 5)

        // WHEN
        val isAmountUpdated = bookRepository.updateAmount(request)

        // THEN
        val updatedBook = bookRepository.findById(savedBook.id)
        assertNotNull(updatedBook, "Updated book should not be null")
        assertTrue(isAmountUpdated, "Amount updated should be true")
        val expected = savedBook.amountAvailable + 5
        assertEquals(expected, updatedBook.amountAvailable, "AmountAvailable should be increased by 5")
    }

    @Test
    fun `updateAmount should decrease amountAvailable when delta is negative and sufficient amountAvailable`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstCreationRequest)
        val request = UpdateAmountRequest(savedBook.id.toString(), -3)

        // WHEN
        val isAmountUpdated = bookRepository.updateAmount(request)

        // THEN
        val updatedBook = bookRepository.findById(savedBook.id)
        assertNotNull(updatedBook, "Updated book should not be null")
        assertTrue(isAmountUpdated, "Amount updated should be true")
        assertEquals(7, updatedBook.amountAvailable, "AmountAvailable should be decreased by 3")
    }

    @Test
    fun `updateAmount should not modify amountAvailable when delta is negative and insufficient amountAvailable`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstCreationRequest)
        val request = UpdateAmountRequest(savedBook.id, -11)

        // WHEN
        val isAmountUpdated = bookRepository.updateAmount(request)

        // THEN
        val updatedBook = bookRepository.findById(savedBook.id)
        assertNotNull(updatedBook, "Updated book should not be null")
        assertFalse(isAmountUpdated, "Amount should not be updated")
        assertEquals(10, updatedBook.amountAvailable, "AmountAvailable should remain unchanged")
    }

    @Test
    fun `updateAmount should set shouldBeNotified to true when amountAvailable equals delta`() {
        // GIVEN
        val bookWithZeroAmount = bookRepository.insert(firstCreationRequest.copy(amountAvailable = 0))
        val request = UpdateAmountRequest(bookWithZeroAmount.id.toString(), 3)

        // WHEN
        val isAmountUpdated = bookRepository.updateAmount(request)

        // THEN
        val updatedBook = bookRepository.findById(bookWithZeroAmount.id.toString())
        assertNotNull(updatedBook, "Updated book should not be null")
        assertTrue(isAmountUpdated, "Amount updated should be true")
        assertEquals(3, updatedBook.amountAvailable, "AmountAvailable should be updated to 3")
    }

    @Test
    fun `updateAmountMany should update multiple books successfully`() {
        // GIVEN
        val firstSavedBook = bookRepository.insert(firstCreationRequest)
        val secondSavedBook = bookRepository.insert(secondCreationRequest)
        val requests = listOf(
            UpdateAmountRequest(firstSavedBook.id.toString(), 5),
            UpdateAmountRequest(secondSavedBook.id.toString(), -3)
        )

        // WHEN
        val matchedCount = bookRepository.updateAmountMany(requests)

        // THEN
        val firstUpdatedBook = bookRepository.findById(firstSavedBook.id.toString())
        assertNotNull(firstUpdatedBook, "Updated book should not be null")
        val secondUpdatedBook = bookRepository.findById(secondSavedBook.id.toString())
        assertNotNull(secondUpdatedBook, "Updated book should not be null")
        assertEquals(2, matchedCount, "Matched count should be equal to requests size")
        assertEquals(15, firstUpdatedBook.amountAvailable, "Book 1 amountAvailable should be updated correctly")
        assertEquals(2, secondUpdatedBook.amountAvailable, "Book 2 amountAvailable should be updated correctly")
    }

    @Test
    fun `updateAmountMany should abort transaction when update fails`() {
        // GIVEN
        val firstSavedBook = bookRepository.insert(firstCreationRequest)
        val secondSavedBook = bookRepository.insert(secondCreationRequest)

        val requests = listOf(
            UpdateAmountRequest(firstSavedBook.id.toString(), 5),
            UpdateAmountRequest(secondSavedBook.id.toString(), -10)
        )

        // WHEN
        val matchedCount = bookRepository.updateAmountMany(requests)

        // THEN
        val firstUpdatedBook = bookRepository.findById(firstSavedBook.id.toString())
        assertNotNull(firstUpdatedBook, "Updated book should not be null")
        val secondUpdatedBook = bookRepository.findById(secondSavedBook.id.toString())
        assertNotNull(secondUpdatedBook, "Updated book should not be null")
        assertEquals(1, matchedCount, "Only first book should match query")
        assertEquals(
            firstCreationRequest.amountAvailable, firstUpdatedBook.amountAvailable,
            "Book 1 should not be updated due to transaction rollback"
        )
        assertEquals(
            secondCreationRequest.amountAvailable, secondUpdatedBook.amountAvailable,
            "Book 2 should not be updated due to transaction rollback"
        )
    }

    @Test
    fun `updateAmountMany should handle case where delta is negative but less than available amount`() {
        // GIVEN
        val firstSavedBook = bookRepository.insert(firstCreationRequest)
        val secondSavedBook = bookRepository.insert(secondCreationRequest)
        val requests = listOf(
            UpdateAmountRequest(firstSavedBook.id.toString(), -2),
            UpdateAmountRequest(secondSavedBook.id.toString(), 3)
        )

        // WHEN
        val matchedCount = bookRepository.updateAmountMany(requests)

        // THEN
        val firstUpdatedBook = bookRepository.findById(firstSavedBook.id.toString())
        assertNotNull(firstUpdatedBook, "Updated book should not be null")
        val secondUpdatedBook = bookRepository.findById(secondSavedBook.id.toString())
        assertNotNull(secondUpdatedBook, "Updated book should not be null")
        assertEquals(2, matchedCount, "Matched count should be equal to requests size")
        assertEquals(8, firstUpdatedBook.amountAvailable, "Book 1 amountAvailable should be decreased correctly")
        assertEquals(8, secondUpdatedBook.amountAvailable, "Book 2 amountAvailable should be increased correctly")
    }

    @Test
    fun `delete should remove book by id`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstCreationRequest)

        // WHEN
        val deletedCount = bookRepository.delete(savedBook.id.toString())

        // THEN
        val actualBook = bookRepository.findById(savedBook.id.toString())
        assertEquals(1L, deletedCount, "Deleted count should be 1")
        assertNull(actualBook, "Book should be deleted from the database")
    }
}
