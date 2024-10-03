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


    private val unsavedUser = User(
        id = null,
        login = "test_user",
        email = "test_user@example.com",
        bookWishList = setOf()
    )

    @Test
    fun `insert should save user and assign id`() {
        // WHEN
        val savedUser = userRepository.insert(unsavedUser)

        // THEN
        assertNotNull(savedUser.id, "Id should not be null after save")
        assertEquals(unsavedUser.login, savedUser.login)
        assertEquals(unsavedUser.email, savedUser.email)
        assertEquals(unsavedUser.bookWishList, savedUser.bookWishList)
    }

    @Test
    fun `findByIdOrNull should return saved user`() {
        // GIVEN
        val savedUser = userRepository.insert(unsavedUser)

        // WHEN
        val actualUser = userRepository.findByIdOrNull(savedUser.id.toString())

        // THEN
        assertNotNull(actualUser, "User should be found")
        assertEquals(savedUser, actualUser)
    }

    @Test
    fun `findByIdOrNull should return null for non-existing user`() {
        // WHEN
        val foundUser = userRepository.findByIdOrNull(ObjectId.get().toHexString())

        // THEN
        assertNull(foundUser, "User should not be found")
    }

    @Test
    fun `update should modify user successfully`() {
        // GIVEN
        val savedUser = userRepository.insert(unsavedUser.copy(login = "old_login"))
        val updatedUser = savedUser.copy(login = "new_login")

        // WHEN
        val modifiedCount = userRepository.update(updatedUser)

        // THEN
        val actualUser = userRepository.findByIdOrNull(savedUser.id.toString())
        assertEquals(1, modifiedCount, "Modified count should be 1")
        assertNotNull(actualUser)
        assertEquals("new_login", actualUser.login, "Login should be updated")
    }


    @Test
    fun `addBookToWishList should add bookId to user's wish list`() {
        // GIVEN
        val savedUser = userRepository.insert(unsavedUser)
        val inputBookId = ObjectId.get().toHexString()

        // WHEN
        val modifiedCount = userRepository.addBookToWishList(savedUser.id.toString(), inputBookId)

        // THEN
        val updatedUser = userRepository.findByIdOrNull(savedUser.id.toString())
        assertEquals(1, modifiedCount, "The matched count should be 1")
        assertNotNull(updatedUser, "Updated user should be found")
        assertTrue(updatedUser.bookWishList.contains(ObjectId(inputBookId)), "The book should be in the user's wish list")
    }

    @Test
    fun `findAllBookSubscribers should return users with matching book in wishList`() {
        // GIVEN
        // Сохраняем несколько книг в базу
        val firstBook = bookRepository.insert(Book(title = "Book One", description = "First book", yearOfPublishing = 2020, price = BigDecimal(10)))
        val secondBook = bookRepository.insert(Book(title = "Book Two", description = "Second book", yearOfPublishing = 2021, price = BigDecimal(15)))

        val firstUser = userRepository.insert(User(login = "firstUser", email = "firstUser@example.com", bookWishList = setOf(firstBook.id!!)))
        val secondUser = userRepository.insert(User(login = "secondUser", email = "secondUser@example.com", bookWishList = setOf(secondBook.id!!, firstBook.id!!)))

        // WHEN
        val result = userRepository.findAllBookSubscribers(firstBook.id!!.toHexString())

        // THEN
        assertEquals(2, result.size)

        val firstUserDetails = result.find { it.login == firstUser.login }
        assertNotNull(firstUserDetails)
        assertEquals(setOf(firstBook.title), firstUserDetails.bookTitles)

        val secondUserDetails = result.find { it.login == secondUser.login }
        assertNotNull(secondUserDetails)
        assertEquals(setOf(firstBook.title), secondUserDetails.bookTitles)
    }

    @Test
    fun `findAllBookSubscribers should return empty list when no users have the book`() {
        // GIVEN
        val firstBook = bookRepository.insert(Book(title = "Book One", description = "First book", yearOfPublishing = 2020, price = BigDecimal(10)))
        val secondBook = bookRepository.insert(Book(title = "Book Two", description = "Second book", yearOfPublishing = 2021, price = BigDecimal(15)))
        userRepository.insert(User(login = "firstUser", email = "firstUser@example.com", bookWishList = setOf(firstBook.id!!)))
        userRepository.insert(User(login = "secondUser", email = "secondUser@example.com", bookWishList = setOf(secondBook.id!!)))

        // WHEN
        val result = userRepository.findAllBookSubscribers(ObjectId.get().toHexString())

        // THEN
        assertTrue(result.isEmpty(), "Expected empty result when no users have the book in their wishList")
    }

    @Test
    fun `findAllBookListSubscribers should return users with correct book titles`() {
        // GIVEN
        val firstBook = bookRepository.insert(Book(title = "Book One", description = "First book", yearOfPublishing = 2020, price = BigDecimal(10)))
        val secondBook = bookRepository.insert(Book(title = "Book Two", description = "Second book", yearOfPublishing = 2021, price = BigDecimal(15)))
        val thirdBook = bookRepository.insert(Book(title = "Book Three", description = "Third book", yearOfPublishing = 2022, price = BigDecimal(20)))

        val firstUser = userRepository.insert(User(login = "user1", email = "user1@example.com", bookWishList = setOf(firstBook.id!!, secondBook.id!!)))
        val secondUser = userRepository.insert(User(login = "user2", email = "user2@example.com", bookWishList = setOf(secondBook.id!!, thirdBook.id!!, firstBook.id!!)))
        val thirdUser = userRepository.insert(User(login = "user3", email = "user3@example.com", bookWishList = setOf(firstBook.id!!)))

        // WHEN
        val result = userRepository.findAllBookListSubscribers(listOf(secondBook.id!!.toHexString(), thirdBook.id!!.toHexString()))

        // THEN
        assertEquals(2, result.size)
        assertNull(result.find { it.login == thirdUser.login })

        val firstUserDetails = result.find { it.login == firstUser.login }
        assertNotNull(firstUserDetails)
        assertEquals(setOf(secondBook.title), firstUserDetails.bookTitles)

        val secondUserDetails = result.find { it.login == secondUser.login }
        assertNotNull(secondUserDetails)
        assertEquals(setOf(secondBook.title, thirdBook.title), secondUserDetails.bookTitles)
    }

    @Test
    fun `findAllBookListSubscribers should return empty list when no matching books`() {
        // GIVEN
        val firstBook = bookRepository.insert(Book(title = "Book One", description = "First book", yearOfPublishing = 2020, price = BigDecimal(10)))
        val secondBook = bookRepository.insert(Book(title = "Book Two", description = "Second book", yearOfPublishing = 2021, price = BigDecimal(15)))

        userRepository.insert(User(login = "user1", email = "user1@example.com", bookWishList = setOf(firstBook.id!!)))
        userRepository.insert(User(login = "user2", email = "user2@example.com", bookWishList = setOf(secondBook.id!!)))

        // WHEN
        val result = userRepository.findAllBookListSubscribers(listOf(ObjectId.get().toHexString()))

        // THEN
        assertTrue(result.isEmpty(), "Expected empty result when no users have the book in their wishList")
    }

    @Test
    fun `delete should remove user by id`() {
        // GIVEN
        val savedUser = userRepository.insert(unsavedUser)

        // WHEN
        val deleteCount = userRepository.delete(savedUser.id.toString())

        // THEN
        val foundUser = userRepository.findByIdOrNull(savedUser.id.toString())
        assertEquals(1, deleteCount, "Deleted count should be 1")
        assertNull(foundUser, "User should not be found after deletion")
    }
}
