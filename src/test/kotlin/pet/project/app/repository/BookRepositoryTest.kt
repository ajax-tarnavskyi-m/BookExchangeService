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
    fun `should save book and assign valid id`() {
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
    fun `should return saved book when queried by id`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstCreationRequest)

        // WHEN
        val actual = bookRepository.findById(savedBook.id)

        // THEN
        assertEquals(savedBook, actual, "Saved book should be equals to retrieved one")
    }

    @Test
    fun `should return true when book exists`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstCreationRequest)

        // WHEN
        val actual = bookRepository.existsById(savedBook.id)

        // THEN
        assertTrue(actual, "Book should exist!")
    }

    @Test
    fun `should return false when book does not exist`() {
        // WHEN
        val actual = bookRepository.existsById(ObjectId().toHexString())

        // THEN
        assertFalse(actual, "Book should not exist!")
    }

    @Test
    fun `should return null when book not found`() {
        // WHEN
        val actual = bookRepository.findById(ObjectId().toHexString())

        // THEN
        assertNull(actual, "Should return null when book does not exist!")
    }

    @Test
    fun `should update shouldBeNotified field and return modified count`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstCreationRequest)

        // WHEN
        val modifiedCount = bookRepository.updateShouldBeNotified(savedBook.id, true)

        // THEN
        assertEquals(1L, modifiedCount, "Modified count should be 1")
    }

    @Test
    fun `should increase amountAvailable when positive delta is applied`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstCreationRequest)
        val adjustment = 5
        val request = UpdateAmountRequest(savedBook.id, adjustment)

        // WHEN
        val isAmountUpdated = bookRepository.updateAmount(request)

        // THEN
        val updatedBook = bookRepository.findById(savedBook.id)
        assertNotNull(updatedBook, "Updated book should not be null")
        assertTrue(isAmountUpdated, "Amount updated should be true")
        val expected = savedBook.amountAvailable + adjustment
        assertEquals(expected, updatedBook.amountAvailable, "AmountAvailable should be increased by 5")
    }

    @Test
    fun `should decrease amountAvailable when negative delta is applied and sufficient amount is available`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstCreationRequest)
        val negativeDelta = -3
        val request = UpdateAmountRequest(savedBook.id, negativeDelta)

        // WHEN
        val isAmountUpdated = bookRepository.updateAmount(request)

        // THEN
        val updatedBook = bookRepository.findById(savedBook.id)
        assertNotNull(updatedBook, "Updated book should not be null")
        assertTrue(isAmountUpdated, "Amount updated should be true")
        val expected = savedBook.amountAvailable + negativeDelta
        assertEquals(expected, updatedBook.amountAvailable, "AmountAvailable should be decreased by 3")
    }

    @Test
    fun `should not modify amountAvailable when insufficient amount is available for negative delta`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstCreationRequest)
        val invalidDelta = -11
        val request = UpdateAmountRequest(savedBook.id, invalidDelta)

        // WHEN
        val isAmountUpdated = bookRepository.updateAmount(request)

        // THEN
        val updatedBook = bookRepository.findById(savedBook.id)
        assertNotNull(updatedBook, "Updated book should not be null")
        assertFalse(isAmountUpdated, "Amount should not be updated")
        val amountBeforeOperation = savedBook.amountAvailable
        assertEquals(amountBeforeOperation, updatedBook.amountAvailable, "AmountAvailable should remain unchanged")
    }

    @Test
    fun `should update multiple books in a transaction successfully`() {
        // GIVEN
        val firstSavedBook = bookRepository.insert(firstCreationRequest)
        val secondSavedBook = bookRepository.insert(secondCreationRequest)
        val positiveDeltaForFirstBook = 5
        val negativeDeltaForSecondBook = -3
        val requests = listOf(
            UpdateAmountRequest(firstSavedBook.id, positiveDeltaForFirstBook),
            UpdateAmountRequest(secondSavedBook.id, negativeDeltaForSecondBook)
        )

        // WHEN
        val matchedCount = bookRepository.updateAmountMany(requests)

        // THEN
        val firstUpdatedBook = bookRepository.findById(firstSavedBook.id)
        assertNotNull(firstUpdatedBook, "Updated book should not be null")
        val secondUpdatedBook = bookRepository.findById(secondSavedBook.id)
        assertNotNull(secondUpdatedBook, "Updated book should not be null")
        assertEquals(requests.size, matchedCount, "Matched count should be equal to requests size")
        val firstExpectedAmount = firstSavedBook.amountAvailable + positiveDeltaForFirstBook
        assertEquals(firstExpectedAmount, firstUpdatedBook.amountAvailable)
        val secondExpectedAmount = secondSavedBook.amountAvailable + negativeDeltaForSecondBook
        assertEquals(secondExpectedAmount, secondUpdatedBook.amountAvailable)
    }

    @Test
    fun `should rollback transaction when update fails for one of the books`() {
        // GIVEN
        val firstSavedBook = bookRepository.insert(firstCreationRequest)
        val secondSavedBook = bookRepository.insert(secondCreationRequest)

        val positiveDelta = 5
        val deltaLessThenAvailable = -10
        val requests = listOf(
            UpdateAmountRequest(firstSavedBook.id, positiveDelta),
            UpdateAmountRequest(secondSavedBook.id, deltaLessThenAvailable)
        )

        // WHEN
        val matchedCount = bookRepository.updateAmountMany(requests)

        // THEN
        val firstUpdatedBook = bookRepository.findById(firstSavedBook.id)
        assertNotNull(firstUpdatedBook, "Updated book should not be null")
        val secondUpdatedBook = bookRepository.findById(secondSavedBook.id)
        assertNotNull(secondUpdatedBook, "Updated book should not be null")
        assertEquals(1, matchedCount, "Only first book should match query")
        assertEquals(
            firstSavedBook.amountAvailable, firstUpdatedBook.amountAvailable,
            "Book 1 should not be updated due to transaction rollback"
        )
        assertEquals(
            secondSavedBook.amountAvailable, secondUpdatedBook.amountAvailable,
            "Book 2 should not be updated due to transaction rollback"
        )
    }

    @Test
    fun `should remove book by id`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstCreationRequest)

        // WHEN
        val deletedCount = bookRepository.delete(savedBook.id)

        // THEN
        val actualBook = bookRepository.findById(savedBook.id)
        assertEquals(1L, deletedCount, "Deleted count should be 1")
        assertNull(actualBook, "Book should be deleted from the database")
    }
}
