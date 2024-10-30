package pet.project.app.mapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pet.project.app.dto.book.UpdateBookRequest
import pet.project.app.mapper.BookMapper.toDomain
import pet.project.app.mapper.BookMapper.toUpdate
import pet.project.app.model.domain.DomainBook
import pet.project.app.model.mongo.MongoBook
import pet.project.core.RandomTestFields.Book.randomAmountAvailable
import pet.project.core.RandomTestFields.Book.randomBookId
import pet.project.core.RandomTestFields.Book.randomDescription
import pet.project.core.RandomTestFields.Book.randomPrice
import pet.project.core.RandomTestFields.Book.randomTitle
import pet.project.core.RandomTestFields.Book.randomYearOfPublishing
import java.math.BigDecimal
import kotlin.test.assertFalse

class BookMapperTest {

    @Test
    fun `should map all fields correctly in toDomain`() {
        // GIVEN
        val mongoBook = MongoBook(
            randomBookId(),
            randomTitle(),
            randomDescription(),
            randomYearOfPublishing(),
            randomPrice(),
            randomAmountAvailable()
        )
        val expected = DomainBook(
            mongoBook.id!!.toHexString(),
            mongoBook.title!!,
            mongoBook.description!!,
            mongoBook.yearOfPublishing!!,
            mongoBook.price!!,
            mongoBook.amountAvailable!!
        )

        // WHEN
        val actual = mongoBook.toDomain()

        // THEN
        assertEquals(expected, actual)
    }

    @Test
    fun `should handle null values correctly in toDomain`() {
        // GIVEN
        val mongoBook = MongoBook()
        val expected = DomainBook("null", "", "", 0, BigDecimal.ZERO, 0)

        // WHEN
        val actual = mongoBook.toDomain()

        // THEN
        assertEquals(expected, actual)
    }

    @Test
    fun `should include all fields when all fields are present in toUpdate`() {
        // GIVEN
        val updateRequest =
            UpdateBookRequest(randomTitle(), randomDescription(), randomYearOfPublishing(), randomPrice())

        // WHEN
        val update = updateRequest.toUpdate()

        // THEN
        assertTrue(update.modifies(MongoBook::title.name))
        assertTrue(update.modifies(MongoBook::description.name))
        assertTrue(update.modifies(MongoBook::yearOfPublishing.name))
        assertTrue(update.modifies(MongoBook::price.name))
    }

    @Test
    fun `should include only non-null fields in toUpdate`() {
        // GIVEN
        val updateRequest = UpdateBookRequest(randomTitle(), null, randomYearOfPublishing(), null)

        // WHEN
        val update = updateRequest.toUpdate()

        // THEN
        assertTrue(update.modifies(MongoBook::title.name))
        assertFalse(update.modifies(MongoBook::description.name))
        assertTrue(update.modifies(MongoBook::yearOfPublishing.name))
        assertFalse(update.modifies(MongoBook::price.name))
    }

    @Test
    fun `should return empty update when all fields are null in toUpdate`() {
        // GIVEN
        val emptyRequest = UpdateBookRequest(null, null, null, null)

        // WHEN
        val update = emptyRequest.toUpdate()

        // THEN
        assertEquals(0, update.updateObject.size, "Update should be empty when all fields are null")
    }
}
