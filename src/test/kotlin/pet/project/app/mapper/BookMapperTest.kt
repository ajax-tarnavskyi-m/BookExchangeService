package pet.project.app.mapper

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pet.project.app.mapper.BookMapper.toDomain
import pet.project.app.model.mongo.MongoBook
import java.math.BigDecimal

class BookMapperTest {

    @Test
    fun `toDomain should map all fields correctly`() {
        // GIVEN
        val mongoBook = MongoBook(
            id = ObjectId.get(),
            title = "Sample Title",
            description = "Sample Description",
            yearOfPublishing = 2021,
            price = BigDecimal("19.99"),
            amountAvailable = 10
        )

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
    fun `toDomain should handle null values correctly`() {
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
}
