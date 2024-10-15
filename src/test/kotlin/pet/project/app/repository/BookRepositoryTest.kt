package pet.project.app.repository

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.book.UpdateAmountRequest
import reactor.test.StepVerifier
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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
        // WHEN & THEN
        StepVerifier.create(bookRepository.insert(firstCreationRequest))
            .consumeNextWith { actualBook ->
                ObjectId.isValid(actualBook.id)
                assertEquals(firstCreationRequest.title, actualBook.title)
                assertEquals(firstCreationRequest.description, actualBook.description)
                assertEquals(firstCreationRequest.yearOfPublishing, actualBook.yearOfPublishing)
                assertEquals(firstCreationRequest.price, actualBook.price)
                assertEquals(firstCreationRequest.amountAvailable, actualBook.amountAvailable)
            }.verifyComplete()
    }

    @Test
    fun `should return saved book when queried by id`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstCreationRequest).block()!!

        // WHEN & THEN
        StepVerifier.create(bookRepository.findById(savedBook.id))
            .expectNext(savedBook).`as`("Saved book should be equals to retrieved one")
            .verifyComplete()
    }

    @Test
    fun `should return true when book exists`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstCreationRequest).block()!!

        // WHEN & THEN
        StepVerifier.create(bookRepository.existsById(savedBook.id))
            .expectNext(true).`as`("Book should exist")
            .verifyComplete()
    }

    @Test
    fun `should return false when book does not exist`() {
        // WHEN & THEN
        StepVerifier.create(bookRepository.existsById(ObjectId().toHexString()))
            .expectNext(false).`as`("Book should not exist")
            .verifyComplete()
    }

    @Test
    fun `should return empty mono when book not found`() {
        // WHEN & THEN
        StepVerifier.create(bookRepository.findById(ObjectId().toHexString()))
            .verifyComplete()
    }

    @Test
    fun `should update shouldBeNotified field and return modified count`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstCreationRequest).block()!!

        // WHEN & THEN
        StepVerifier.create(bookRepository.updateShouldBeNotified(savedBook.id, true))
            .expectNext(1L).`as`("One record should be modified")
            .verifyComplete()
    }

    @Test
    fun `should increase amountAvailable when positive delta is applied`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstCreationRequest).block()!!
        val adjustment = 5
        val request = UpdateAmountRequest(savedBook.id, adjustment)

        // WHEN & THEN
        StepVerifier.create(bookRepository.updateAmount(request))
            .expectNext(true).`as`("Should return true when amount updated")
            .verifyComplete()

        // THEN
        val updatedBook = bookRepository.findById(savedBook.id).block()!!
        assertNotNull(updatedBook, "Updated book should not be null")
        val expected = savedBook.amountAvailable + adjustment
        assertEquals(expected, updatedBook.amountAvailable, "AmountAvailable should be increased by 5")
    }

    @Test
    fun `should decrease amountAvailable when negative delta is applied and sufficient amount is available`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstCreationRequest).block()!!
        val negativeDelta = -3
        val request = UpdateAmountRequest(savedBook.id, negativeDelta)

        // WHEN & THEN
        StepVerifier.create(bookRepository.updateAmount(request))
            .expectNext(true).`as`("Should return true when amount updated")
            .verifyComplete()

        // THEN
        val updatedBook = bookRepository.findById(savedBook.id).block()
        assertNotNull(updatedBook, "Updated book should not be null")
        val expected = savedBook.amountAvailable + negativeDelta
        assertEquals(expected, updatedBook.amountAvailable, "AmountAvailable should be decreased by 3")
    }

    @Test
    fun `should not modify amountAvailable when insufficient amount is available for negative delta`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstCreationRequest).block()!!
        val invalidDelta = -11
        val request = UpdateAmountRequest(savedBook.id, invalidDelta)

        // WHEN & THEN
        StepVerifier.create(bookRepository.updateAmount(request))
            .expectNext(false).`as`("Should be false when no records was affected")
            .verifyComplete()

        // THEN
        val updatedBook = bookRepository.findById(savedBook.id).block()
        assertNotNull(updatedBook, "Updated book should not be null")
        val amountBeforeOperation = savedBook.amountAvailable
        assertEquals(amountBeforeOperation, updatedBook.amountAvailable, "AmountAvailable should remain unchanged")
    }

    @Test
    fun `should update multiple books in a transaction successfully`() {
        // GIVEN
        val firstSavedBook = bookRepository.insert(firstCreationRequest).block()!!
        val secondSavedBook = bookRepository.insert(secondCreationRequest).block()!!
        val positiveDeltaForFirstBook = 5
        val negativeDeltaForSecondBook = -3
        val requests = listOf(
            UpdateAmountRequest(firstSavedBook.id, positiveDeltaForFirstBook),
            UpdateAmountRequest(secondSavedBook.id, negativeDeltaForSecondBook)
        )

        // WHEN & THEN
        StepVerifier.create(bookRepository.updateAmountMany(requests))
            .expectNext(requests.size).`as`("Matched count should be equal to requests size")
            .verifyComplete()

        // THEN
        val firstUpdatedBook = bookRepository.findById(firstSavedBook.id).block()
        assertNotNull(firstUpdatedBook, "Updated book should not be null")
        val secondUpdatedBook = bookRepository.findById(secondSavedBook.id).block()
        assertNotNull(secondUpdatedBook, "Updated book should not be null")
        val firstExpectedAmount = firstSavedBook.amountAvailable + positiveDeltaForFirstBook
        assertEquals(firstExpectedAmount, firstUpdatedBook.amountAvailable)
        val secondExpectedAmount = secondSavedBook.amountAvailable + negativeDeltaForSecondBook
        assertEquals(secondExpectedAmount, secondUpdatedBook.amountAvailable)
    }

    @Test
    fun `should rollback transaction when update fails for one of the books`() {
        // GIVEN
        val firstSavedBook = bookRepository.insert(firstCreationRequest).block()!!
        val secondSavedBook = bookRepository.insert(secondCreationRequest).block()!!

        val positiveDelta = 5
        val deltaLessThenAvailable = -10
        val requests = listOf(
            UpdateAmountRequest(firstSavedBook.id, positiveDelta),
            UpdateAmountRequest(secondSavedBook.id, deltaLessThenAvailable)
        )

        // WHEN
        StepVerifier.create(bookRepository.updateAmountMany(requests))
            .consumeErrorWith { ex ->
                assertEquals(IllegalArgumentException::class.java, ex.javaClass)
                assertEquals("Not existing ids or not enough books: $requests", ex.message)
            }
            .verify()

        // THEN
        val firstUpdatedBook = bookRepository.findById(firstSavedBook.id).block()
        assertNotNull(firstUpdatedBook, "Updated book should not be null")
        val secondUpdatedBook = bookRepository.findById(secondSavedBook.id).block()
        assertNotNull(secondUpdatedBook, "Updated book should not be null")
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
        val savedBook = bookRepository.insert(firstCreationRequest).block()!!

        // WHEN & THEN
        StepVerifier.create(bookRepository.delete(savedBook.id))
            .expectNext(1L).`as`("Should return delete count of one affected record")
            .verifyComplete()

        // THEN
        val actualBook = bookRepository.findById(savedBook.id).block()
        assertNull(actualBook, "Book should be deleted from the database")
    }
}
