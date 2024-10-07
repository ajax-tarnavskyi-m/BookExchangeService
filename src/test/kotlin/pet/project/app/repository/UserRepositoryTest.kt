package pet.project.app.repository

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.user.CreateUserRequest
import pet.project.app.dto.user.UpdateUserRequest
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

    private val firstCreateUserRequest = CreateUserRequest(login = "test_user", email = "test_user@example.com")
    private val secondUserCreateRequest = CreateUserRequest(login = "secondUser", email = "secondUser@example.com")
    private val firstCreateBookRequest = CreateBookRequest("Book One", "First book", 2020, BigDecimal(10), 0)
    private val secondCreateBookRequest = CreateBookRequest("Book Two", "Second book", 2021, BigDecimal(15), 0)
    private val thirdCreateBookRequest = CreateBookRequest("Book Three", "Third book", 2022, BigDecimal(20), 0)

    @Test
    fun `should save user and assign id`() {
        // WHEN
        val savedUser = userRepository.insert(firstCreateUserRequest)

        // THEN
        assertNotNull(savedUser.id, "Id should not be null after save")
        assertEquals(firstCreateUserRequest.login, savedUser.login)
        assertEquals(firstCreateUserRequest.email, savedUser.email)
        assertEquals(firstCreateUserRequest.bookWishList, savedUser.bookWishList)
    }

    @Test
    fun `should return saved user by id`() {
        // GIVEN
        val savedUser = userRepository.insert(firstCreateUserRequest)

        // WHEN
        val actualUser = userRepository.findById(savedUser.id.toString())

        // THEN
        assertNotNull(actualUser, "User should be found")
        assertEquals(savedUser, actualUser)
    }

    @Test
    fun `should return null for non-existing user`() {
        // WHEN
        val foundUser = userRepository.findById(ObjectId.get().toHexString())

        // THEN
        assertNull(foundUser, "User should not be found")
    }

    @Test
    fun `should update user fields successfully`() {
        // GIVEN
        val savedUser = userRepository.insert(firstCreateUserRequest.copy(login = "old_login"))
        val updateRequest = UpdateUserRequest(login = "new_login", null, null)

        // WHEN
        val updatedUser = userRepository.update(savedUser.id, updateRequest)

        // THEN
        assertNotNull(updatedUser)
        assertEquals("new_login", updatedUser.login, "Login should be updated")
    }

    @Test
    fun `should return null if user does not exist during update`() {
        // GIVEN
        val nonExistentUserId = "nonexistent_user_id"
        val updateRequest = UpdateUserRequest(login = "new_login", email = null, bookWishList = null)

        // WHEN
        val result = userRepository.update(nonExistentUserId, updateRequest)

        // THEN
        assertNull(result, "Result should be null if the user is not found")
    }

    @Test
    fun `should add book id to user's wishlist`() {
        // GIVEN
        val savedUser = userRepository.insert(firstCreateUserRequest)
        val inputBookId = ObjectId.get().toHexString()

        // WHEN
        val modifiedCount = userRepository.addBookToWishList(savedUser.id, inputBookId)

        // THEN
        val updatedUser = userRepository.findById(savedUser.id)
        assertEquals(1, modifiedCount, "The matched count should be 1")
        assertNotNull(updatedUser, "Updated user should be found")
        assertTrue(
            updatedUser.bookWishList.contains(inputBookId),
            "The book should be in the user's wish list"
        )
    }

    @Test
    fun `should return users with matching book in wishlist`() {
        // GIVEN
        val firstBook = bookRepository.insert(firstCreateBookRequest)
        val secondBook = bookRepository.insert(secondCreateBookRequest)
        val firstUser = userRepository.insert(
            firstCreateUserRequest.copy(bookWishList = setOf(secondBook.id, firstBook.id))
        )
        val secondUser = userRepository.insert(
            secondUserCreateRequest.copy(bookWishList = setOf(firstBook.id, secondBook.id))
        )

        // WHEN
        val result = userRepository.findAllBookSubscribers(firstBook.id)

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
    fun `should return empty list when no users have the book in wishlist`() {
        // GIVEN
        val firstBook = bookRepository.insert(firstCreateBookRequest)
        val secondBook = bookRepository.insert(secondCreateBookRequest)
        userRepository.insert(firstCreateUserRequest.copy(bookWishList = setOf(firstBook.id)))
        userRepository.insert(secondUserCreateRequest.copy(bookWishList = setOf(secondBook.id)))

        // WHEN
        val result = userRepository.findAllBookSubscribers(ObjectId.get().toHexString())

        // THEN
        assertTrue(result.isEmpty(), "Expected empty result when no users have the book in their wishList")
    }

    @Test
    fun `should return users with correct book titles in their wishlist`() {
        // GIVEN
        val firstBook = bookRepository.insert(firstCreateBookRequest)
        val secondBook = bookRepository.insert(secondCreateBookRequest)
        val thirdBook = bookRepository.insert(thirdCreateBookRequest)
        val firstUser = userRepository.insert(
            firstCreateUserRequest.copy(bookWishList = setOf(firstBook.id, secondBook.id))
        )
        val secondUser = userRepository.insert(
            secondUserCreateRequest.copy(bookWishList = setOf(secondBook.id, thirdBook.id, firstBook.id))
        )
        val thirdDomainUser = userRepository.insert(
            CreateUserRequest(login = "user3", email = "user3@example.com", bookWishList = setOf(firstBook.id))
        )
        val requestIds = listOf(secondBook.id, thirdBook.id)

        // WHEN
        val result = userRepository.findAllBookListSubscribers(requestIds)

        // THEN
        assertEquals(requestIds.size, result.size)
        assertNull(result.find { it.login == thirdDomainUser.login })

        val firstUserDetails = result.find { it.login == firstUser.login }
        assertNotNull(firstUserDetails, "User details should not be null")
        assertEquals(setOf(secondBook.title), firstUserDetails.bookTitles)

        val secondUserDetails = result.find { it.login == secondUser.login }
        assertNotNull(secondUserDetails, "User details should not be null")
        assertEquals(setOf(secondBook.title, thirdBook.title), secondUserDetails.bookTitles)
    }

    @Test
    fun `should return empty list when no matching books are found in wishlist`() {
        // GIVEN
        val firstBook = bookRepository.insert(firstCreateBookRequest)
        val secondBook = bookRepository.insert(firstCreateBookRequest)

        userRepository.insert(firstCreateUserRequest.copy(bookWishList = setOf(firstBook.id)))
        userRepository.insert(secondUserCreateRequest.copy(bookWishList = setOf(secondBook.id)))

        // WHEN
        val result = userRepository.findAllBookListSubscribers(listOf(ObjectId.get().toHexString()))

        // THEN
        assertTrue(result.isEmpty(), "Expected empty result when no users have the book in their wishList")
    }

    @Test
    fun `should remove user by id`() {
        // GIVEN
        val savedUser = userRepository.insert(firstCreateUserRequest)

        // WHEN
        val deleteCount = userRepository.delete(savedUser.id.toString())

        // THEN
        val foundUser = userRepository.findById(savedUser.id.toString())
        assertEquals(1, deleteCount, "Deleted count should be 1")
        assertNull(foundUser, "User should not be found after deletion")
    }
}
