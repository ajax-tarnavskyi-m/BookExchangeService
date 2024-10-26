package pet.project.app.repository

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.core.RandomTestFields.Book.amountAvailable
import pet.project.core.RandomTestFields.Book.description
import pet.project.core.RandomTestFields.Book.price
import pet.project.core.RandomTestFields.Book.title
import pet.project.core.RandomTestFields.Book.yearOfPublishing
import pet.project.core.RandomTestFields.SecondBook.secondAmountAvailable
import pet.project.core.RandomTestFields.SecondBook.secondDescription
import pet.project.core.RandomTestFields.SecondBook.secondPrice
import pet.project.core.RandomTestFields.SecondBook.secondTitle
import pet.project.core.RandomTestFields.SecondBook.secondYearOfPublishing
import reactor.kotlin.test.test
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BookRepositoryTest : AbstractTestContainer {
    @Autowired
    private lateinit var bookRepository: BookRepository

    private val firstCreationRequest = CreateBookRequest(title, description, yearOfPublishing, price, amountAvailable)

    private val secondCreationRequest = CreateBookRequest(
        secondTitle,
        secondDescription,
        secondYearOfPublishing,
        secondPrice,
        secondAmountAvailable,
    )

    @Test
    fun `should save book and assign valid id`() {
        // WHEN
        val actualMono = bookRepository.insert(firstCreationRequest)

        // THEN
        actualMono.test()
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

        // WHEN
        val actualMono = bookRepository.findById(savedBook.id)

        // THEN
        actualMono.test().expectNext(savedBook).`as`("Saved book should be equals to retrieved one")
            .verifyComplete()
    }

    @Test
    fun `should return true when book exists`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstCreationRequest).block()!!

        // WHEN
        val actualMono = bookRepository.existsById(savedBook.id)

        // THEN
        actualMono.test()
            .expectNext(true).`as`("Book should exist")
            .verifyComplete()
    }

    @Test
    fun `should return false when book does not exist`() {
        // WHEN
        val actualMono = bookRepository.existsById(ObjectId().toHexString())

        // THEN
        actualMono.test()
            .expectNext(false).`as`("Book should not exist")
            .verifyComplete()
    }

    @Test
    fun `should return empty mono when book not found`() {
        // WHEN
        val actualMono = bookRepository.findById(ObjectId().toHexString())
        actualMono.test().verifyComplete()
    }

    @Test
    fun `should update shouldBeNotified field and return modified count`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstCreationRequest).block()!!

        // WHEN
        val actualMono = bookRepository.updateShouldBeNotified(savedBook.id, true)

        // THEN
        actualMono.test()
            .expectNext(1L).`as`("One record should be modified")
            .verifyComplete()
    }

    @Test
    fun `should increase amountAvailable when positive delta is applied`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstCreationRequest).block()!!
        val adjustment = 5
        val request = UpdateAmountRequest(savedBook.id, adjustment)

        // WHEN
        val actualMono = bookRepository.updateAmount(request)

        // THEN
        actualMono.test()
            .expectNext(true).`as`("Should return true when amount updated")
            .verifyComplete()

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

        // WHEN
        val actualMono = bookRepository.updateAmount(request)

        // THEN
        actualMono.test()
            .expectNext(true).`as`("Should return true when amount updated")
            .verifyComplete()

        val updatedBook = bookRepository.findById(savedBook.id).block()
        assertNotNull(updatedBook, "Updated book should not be null")
        val expected = savedBook.amountAvailable + negativeDelta
        assertEquals(expected, updatedBook.amountAvailable, "AmountAvailable should be decreased by 3")
    }

    @Test
    fun `should not modify amountAvailable when insufficient amount is available for negative delta`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstCreationRequest).block()!!
        val lessThenAvailableDelta = -firstCreationRequest.amountAvailable - 1
        val request = UpdateAmountRequest(savedBook.id, lessThenAvailableDelta)

        // WHEN
        val actualMono = bookRepository.updateAmount(request)

        // THEN
        actualMono.test()
            .expectNext(false).`as`("Should be false when no records was affected")
            .verifyComplete()

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
        val negativeDeltaForSecondBook = -secondCreationRequest.amountAvailable
        val requests = listOf(
            UpdateAmountRequest(firstSavedBook.id, positiveDeltaForFirstBook),
            UpdateAmountRequest(secondSavedBook.id, negativeDeltaForSecondBook)
        )

        // WHEN
        val actualMono = bookRepository.updateAmountMany(requests)

        // THEN
        actualMono.test()
            .expectNext(requests.size).`as`("Matched count should be equal to requests size")
            .verifyComplete()

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
        val deltaLessThenAvailable = -secondCreationRequest.amountAvailable - 1
        val requests = listOf(
            UpdateAmountRequest(firstSavedBook.id, positiveDelta),
            UpdateAmountRequest(secondSavedBook.id, deltaLessThenAvailable)
        )

        // WHEN
        val actualMono = bookRepository.updateAmountMany(requests)

        // THEN
        actualMono.test()
            .consumeErrorWith { ex ->
                assertEquals(IllegalArgumentException::class.java, ex.javaClass)
                assertEquals("Not existing ids or not enough books: $requests", ex.message)
            }
            .verify()

        val firstUpdatedBook = bookRepository.findById(firstSavedBook.id).block()
        assertNotNull(firstUpdatedBook, "Updated book should not be null")
        val secondUpdatedBook = bookRepository.findById(secondSavedBook.id).block()
        assertNotNull(secondUpdatedBook, "Updated book should not be null")
        assertEquals(
            firstSavedBook.amountAvailable,
            firstUpdatedBook.amountAvailable,
            "Book 1 should not be updated due to transaction rollback"
        )
        assertEquals(
            secondSavedBook.amountAvailable,
            secondUpdatedBook.amountAvailable,
            "Book 2 should not be updated due to transaction rollback"
        )
    }

    @Test
    fun `should remove book by id`() {
        // GIVEN
        val savedBook = bookRepository.insert(firstCreationRequest).block()!!

        // WHEN
        val actualMono = bookRepository.delete(savedBook.id)

        // THEN
        actualMono.test()
            .expectNext(1L).`as`("Should return delete count of one affected record")
            .verifyComplete()

        val actualBook = bookRepository.findById(savedBook.id).block()
        assertNull(actualBook, "Book should be deleted from the database")
    }
}
