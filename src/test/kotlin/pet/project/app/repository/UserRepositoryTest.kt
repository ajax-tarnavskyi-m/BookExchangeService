package pet.project.app.repository

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import pet.project.app.model.Book
import pet.project.app.model.User
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserRepositoryTest : AbstractMongoTestContainer {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var bookRepository: BookRepository

    private val firstUnsavedUser = User(login = "test_user", email = "test_user@example.com")
    private val secondUnsavedUser = User(login = "secondUser", email = "secondUser@example.com")
    private val firstUnsavedBook = Book(
        title = "Book One", description = "First book", yearOfPublishing = 2020, price = BigDecimal(10)
    )
    private val secondUnsavedBook = Book(
        title = "Book Two", description = "Second book", yearOfPublishing = 2021, price = BigDecimal(15)
    )
    private val thirdUnsavedBook = Book(
        title = "Book Three", description = "Third book", yearOfPublishing = 2022, price = BigDecimal(20)
    )

    @Test
    fun `insert should save user and assign id`() {
        // WHEN
        val savedUser = userRepository.insert(firstUnsavedUser)

        // THEN
        assertNotNull(savedUser.id, "Id should not be null after save")
        assertEquals(firstUnsavedUser.login, savedUser.login)
        assertEquals(firstUnsavedUser.email, savedUser.email)
        assertEquals(firstUnsavedUser.bookWishList, savedUser.bookWishList)
    }

    @Test
    fun `findByIdOrNull should return saved user`() {
        // GIVEN
        val savedUser = userRepository.insert(firstUnsavedUser)

        // WHEN
        val actualUser = userRepository.findById(savedUser.id.toString())

        // THEN
        assertNotNull(actualUser, "User should be found")
        assertEquals(savedUser, actualUser)
    }

    @Test
    fun `findByIdOrNull should return null for non-existing user`() {
        // WHEN
        val foundUser = userRepository.findById(ObjectId.get().toHexString())

        // THEN
        assertNull(foundUser, "User should not be found")
    }

    @Test
    fun `update should modify user successfully`() {
        // GIVEN
        val savedUser = userRepository.insert(firstUnsavedUser.copy(login = "old_login"))
        val updatedUser = savedUser.copy(login = "new_login")

        // WHEN
        val modifiedCount = userRepository.update(updatedUser)

        // THEN
        val actualUser = userRepository.findById(savedUser.id.toString())
        assertEquals(1, modifiedCount, "Modified count should be 1")
        assertNotNull(actualUser)
        assertEquals("new_login", actualUser.login, "Login should be updated")
    }


    @Test
    fun `addBookToWishList should add bookId to user's wish list`() {
        // GIVEN
        val savedUser = userRepository.insert(firstUnsavedUser)
        val inputBookId = ObjectId.get().toHexString()

        // WHEN
        val modifiedCount = userRepository.addBookToWishList(savedUser.id.toString(), inputBookId)

        // THEN
        val updatedUser = userRepository.findById(savedUser.id.toString())
        assertEquals(1, modifiedCount, "The matched count should be 1")
        assertNotNull(updatedUser, "Updated user should be found")
        assertTrue(
            updatedUser.bookWishList.contains(ObjectId(inputBookId)),
            "The book should be in the user's wish list"
        )
    }

    @Test
    fun `findAllBookSubscribers should return users with matching book in wishList`() {
        // GIVEN
        val firstBook = bookRepository.insert(firstUnsavedBook)
        val secondBook = bookRepository.insert(secondUnsavedBook)
        val firstUser = userRepository.insert(
            firstUnsavedUser.copy(bookWishList = setOf(secondBook.id!!, firstBook.id!!))
        )
        val secondUser = userRepository.insert(
            secondUnsavedUser.copy(bookWishList = setOf(firstBook.id!!, secondBook.id!!))
        )

        // WHEN
        val result = userRepository.findAllBookSubscribers(firstBook.id!!.toHexString())

        // THEN
        assertEquals(2, result.size, "User details should contains both users info")

        val firstUserDetails = result.find { it.login == firstUser.login }
        assertNotNull(firstUserDetails, "User details should not be null")
        assertEquals(setOf(firstBook.title), firstUserDetails.bookTitles)

        val secondUserDetails = result.find { it.login == secondUser.login }
        assertNotNull(secondUserDetails, "User details should not be null")
        assertEquals(setOf(firstBook.title), secondUserDetails.bookTitles)
    }

    @Test
    fun `findAllBookSubscribers should return empty list when no users have the book`() {
        // GIVEN
        val firstBook = bookRepository.insert(firstUnsavedBook)
        val secondBook = bookRepository.insert(secondUnsavedBook)
        userRepository.insert(firstUnsavedUser.copy(bookWishList = setOf(firstBook.id!!)))
        userRepository.insert(secondUnsavedUser.copy(bookWishList = setOf(secondBook.id!!)))

        // WHEN
        val result = userRepository.findAllBookSubscribers(ObjectId.get().toHexString())

        // THEN
        assertTrue(result.isEmpty(), "Expected empty result when no users have the book in their wishList")
    }

    @Test
    fun `findAllBookListSubscribers should return users with correct book titles`() {
        // GIVEN
        val firstBook = bookRepository.insert(firstUnsavedBook)
        val secondBook = bookRepository.insert(secondUnsavedBook)
        val thirdBook = bookRepository.insert(thirdUnsavedBook)
        val firstUser = userRepository.insert(
            firstUnsavedUser.copy(bookWishList = setOf(firstBook.id!!, secondBook.id!!))
        )
        val secondUser = userRepository.insert(
            secondUnsavedUser.copy(bookWishList = setOf(secondBook.id!!, thirdBook.id!!, firstBook.id!!))
        )
        val thirdUser = userRepository.insert(
            User(login = "user3", email = "user3@example.com", bookWishList = setOf(firstBook.id!!))
        )
        val requestIds = listOf(secondBook.id!!.toHexString(), thirdBook.id!!.toHexString())

        // WHEN
        val result = userRepository.findAllBookListSubscribers(requestIds)

        // THEN
        assertEquals(2, result.size)
        assertNull(result.find { it.login == thirdUser.login })

        val firstUserDetails = result.find { it.login == firstUser.login }
        assertNotNull(firstUserDetails, "User details should not be null")
        assertEquals(setOf(secondBook.title), firstUserDetails.bookTitles)

        val secondUserDetails = result.find { it.login == secondUser.login }
        assertNotNull(secondUserDetails, "User details should not be null")
        assertEquals(setOf(secondBook.title, thirdBook.title), secondUserDetails.bookTitles)
    }

    @Test
    fun `findAllBookListSubscribers should return empty list when no matching books`() {
        // GIVEN
        val firstBook = bookRepository.insert(firstUnsavedBook)
        val secondBook = bookRepository.insert(firstUnsavedBook)

        userRepository.insert(firstUnsavedUser.copy(bookWishList = setOf(firstBook.id!!)))
        userRepository.insert(secondUnsavedUser.copy(bookWishList = setOf(secondBook.id!!)))

        // WHEN
        val result = userRepository.findAllBookListSubscribers(listOf(ObjectId.get().toHexString()))

        // THEN
        assertTrue(result.isEmpty(), "Expected empty result when no users have the book in their wishList")
    }

    @Test
    fun `delete should remove user by id`() {
        // GIVEN
        val savedUser = userRepository.insert(firstUnsavedUser)

        // WHEN
        val deleteCount = userRepository.delete(savedUser.id.toString())

        // THEN
        val foundUser = userRepository.findById(savedUser.id.toString())
        assertEquals(1, deleteCount, "Deleted count should be 1")
        assertNull(foundUser, "User should not be found after deletion")
    }
}
