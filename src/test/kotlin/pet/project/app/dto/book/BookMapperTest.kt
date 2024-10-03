package pet.project.app.dto.book
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pet.project.app.model.Book
import java.math.BigDecimal

class BookMapperTest {

    private val mapper = BookMapper()

    @Test
    fun `toDto should map Book to ResponseBookDto correctly`() {
        // GIVEN
        val book = Book(
            ObjectId("507f1f77bcf86cd799439011"),
            "Test Book",
            "Test Description",
            2023,
            BigDecimal(20.99),
            10
        )
        val expectedDto = ResponseBookDto(
            "507f1f77bcf86cd799439011",
            "Test Book",
            "Test Description",
            2023,
            BigDecimal(20.99),
            10
        )

        // WHEN
        val actualDto = mapper.toDto(book)

        // THEN
        assertEquals(expectedDto, actualDto)
    }

    @Test
    fun `toModel should map CreateBookRequest to Book correctly`() {
        // GIVEN
        val request = CreateBookRequest(
            "Test Book",
            "Test Description",
            2023,
            BigDecimal(20.99),
            10
        )
        val expectedBook = Book(
            null,
            "Test Book",
            "Test Description",
            2023,
            BigDecimal(20.99),
            10
        )

        // WHEN
        val actualBook = mapper.toModel(request)

        // THEN
        assertEquals(expectedBook, actualBook)
    }

    @Test
    fun `toModel should map UpdateBookRequest to Book correctly`() {
        // GIVEN
        val request = UpdateBookRequest(
            "507f1f77bcf86cd799439011",
            "Updated Book",
            "Updated Description",
            2024,
            BigDecimal(20.99),
            15
        )
        val expectedBook = Book(
            ObjectId("507f1f77bcf86cd799439011"),
            "Updated Book",
            "Updated Description",
            2024,
            BigDecimal(20.99),
            15
        )

        // WHEN
        val actualBook = mapper.toModel(request)

        // THEN
        assertEquals(expectedBook, actualBook)
    }

    @Test
    fun `toDto should handle null and default values correctly`() {
        // GIVEN
        val book = Book(
            ObjectId("507f1f77bcf86cd799439011"),
            "",
            "Test Description",
            null,
            BigDecimal(20.99),
        )
        val expectedDto = ResponseBookDto(
            "507f1f77bcf86cd799439011",
            "",
            "Test Description",
            0,
            BigDecimal(20.99),
            0
        )

        // WHEN
        val actualDto = mapper.toDto(book)

        // THEN
        assertEquals(expectedDto, actualDto)
    }
}
