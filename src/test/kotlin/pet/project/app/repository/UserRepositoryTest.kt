package pet.project.app.repository

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.user.CreateUserRequest
import pet.project.app.dto.user.UpdateUserRequest
import pet.project.app.dto.user.UserNotificationDetails
import reactor.kotlin.test.test
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
        val actualMono = userRepository.insert(firstCreateUserRequest)

        // THEN
        actualMono.test()
            .consumeNextWith { savedUser ->
                assertNotNull(savedUser.id, "Id should not be null after save")
                assertEquals(firstCreateUserRequest.login, savedUser.login)
                assertEquals(firstCreateUserRequest.email, savedUser.email)
                assertEquals(firstCreateUserRequest.bookWishList, savedUser.bookWishList)
            }
            .verifyComplete()
    }

    @Test
    fun `should return saved user by id`() {
        // GIVEN
        val savedUser = userRepository.insert(firstCreateUserRequest).block()!!

        // WHEN
        val actualMono = userRepository.findById(savedUser.id)

        // THEN
        actualMono.test()
            .expectNext(savedUser)
            .verifyComplete()
    }

    @Test
    fun `should return empty mono for non-existing user`() {
        // WHEN
        val actualMono = userRepository.findById(ObjectId.get().toHexString())

        // THEN
        actualMono.test().verifyComplete()
    }

    @Test
    fun `should update user fields successfully`() {
        // GIVEN
        val savedUser = userRepository.insert(firstCreateUserRequest.copy(login = "old_login")).block()!!
        val updateRequest = UpdateUserRequest(login = "new_login", null, null)
        val expectedUpdatedUser = savedUser.copy(login = "new_login")

        // WHEN
        val actualMono = userRepository.update(savedUser.id, updateRequest)

        // THEN
        actualMono.test()
            .expectNext(expectedUpdatedUser)
            .verifyComplete()
    }

    @Test
    fun `should return empty mono if user does not exist during update`() {
        // GIVEN
        val nonExistentUserId = "nonexistent_user_id"
        val updateRequest = UpdateUserRequest(login = "new_login", email = null, bookWishList = null)

        // WHEN
        val actualMono = userRepository.update(nonExistentUserId, updateRequest)

        // THEN
        actualMono.test().verifyComplete()
    }

    @Test
    fun `should add book id to user's wishlist`() {
        // GIVEN
        val savedUser = userRepository.insert(firstCreateUserRequest).block()!!
        val inputBookId = ObjectId.get().toHexString()
        val expectedModifiedCount = 1L

        // WHEN
        val actualMono = userRepository.addBookToWishList(savedUser.id, inputBookId)

        // THEN
        actualMono.test()
            .expectNext(expectedModifiedCount)
            .verifyComplete()
        val updatedUser = userRepository.findById(savedUser.id).block()!!
        assertNotNull(updatedUser, "Updated user should be found")
        assertTrue(
            updatedUser.bookWishList.contains(inputBookId),
            "The book should be in the user's wish list"
        )
    }

    @Test
    fun `should return users with correct book titles in their wishlist`() {
        // GIVEN
        val firstBook = bookRepository.insert(firstCreateBookRequest).block()!!
        val secondBook = bookRepository.insert(secondCreateBookRequest).block()!!
        val thirdBook = bookRepository.insert(thirdCreateBookRequest).block()!!
        val firstUser = userRepository.insert(
            firstCreateUserRequest.copy(bookWishList = setOf(firstBook.id, secondBook.id))
        ).block()!!
        val secondUser = userRepository.insert(
            secondUserCreateRequest.copy(bookWishList = setOf(secondBook.id, thirdBook.id, firstBook.id))
        ).block()!!
        val requestIds = listOf(secondBook.id, thirdBook.id)
        val firstExpected = UserNotificationDetails(firstUser.login, firstUser.email, setOf(secondBook.title))
        val secondExpected = UserNotificationDetails(
            secondUser.login, secondUser.email, setOf(secondBook.title, thirdBook.title)
        )

        // WHEN
        val actualFlux = userRepository.findAllSubscribersOf(requestIds)

        // THEN
        actualFlux.test()
            .expectNext(firstExpected, secondExpected)
            .verifyComplete()
    }

    @Test
    fun `should return empty flux when no matching books are found in wishlist`() {
        // GIVEN
        val firstBook = bookRepository.insert(firstCreateBookRequest).block()!!
        val secondBook = bookRepository.insert(firstCreateBookRequest).block()!!

        userRepository.insert(firstCreateUserRequest.copy(bookWishList = setOf(firstBook.id))).block()!!
        userRepository.insert(secondUserCreateRequest.copy(bookWishList = setOf(secondBook.id))).block()!!

        // WHEN
        val actualFlux = userRepository.findAllSubscribersOf(listOf(ObjectId.get().toHexString()))

        // THEN
        actualFlux.test().verifyComplete()
    }

    @Test
    fun `should remove user by id`() {
        // GIVEN
        val savedUser = userRepository.insert(firstCreateUserRequest).block()!!
        val expectedDeleteCount = 1L

        // WHEN
        val actualMono = userRepository.delete(savedUser.id)

        // THEN
        actualMono.test()
            .expectNext(expectedDeleteCount)
            .verifyComplete()
        val foundUser = userRepository.findById(savedUser.id).block()
        assertNull(foundUser, "User should not be found after deletion")
    }
}
