package pet.project.app.mapper

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pet.project.app.dto.book.UpdateBookRequest
import pet.project.app.mapper.BookMapper.toDomain
import pet.project.app.mapper.BookMapper.toUpdate
import pet.project.app.model.mongo.MongoBook
import java.math.BigDecimal
import kotlin.test.assertFalse

class BookMapperTest {

    @Test
    fun `should map all fields correctly in toDomain`() {
        // GIVEN
        val mongoBook = MongoBook(ObjectId.get(), "Sample Title", "Sample Description", 2021, BigDecimal("19.99"), 10)

        // WHEN
        val domainBook = mongoBook.toDomain()

        // THEN
        assertEquals(mongoBook.id.toString(), domainBook.id)
        assertEquals(mongoBook.title, domainBook.title)
        assertEquals(mongoBook.description, domainBook.description)
        assertEquals(mongoBook.yearOfPublishing, domainBook.yearOfPublishing)
        assertEquals(mongoBook.price, domainBook.price)
        assertEquals(mongoBook.amountAvailable, domainBook.amountAvailable)
    }

    @Test
    fun `should handle null values correctly in toDomain`() {
        // GIVEN
        val mongoBook = MongoBook()

        // WHEN
        val domainBook = mongoBook.toDomain()

        // THEN
        assertEquals("", domainBook.title)
        assertEquals("", domainBook.description)
        assertEquals(0, domainBook.yearOfPublishing)
        assertEquals(BigDecimal.ZERO, domainBook.price)
        assertEquals(0, domainBook.amountAvailable)
    }

    @Test
    fun `should include all fields when all fields are present in toUpdate`() {
        // GIVEN
        val updateRequest = UpdateBookRequest("New Title", "New Description", 2021, BigDecimal("19.99"))

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
        val updateRequest = UpdateBookRequest("New Title", null, 2021, null)

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
