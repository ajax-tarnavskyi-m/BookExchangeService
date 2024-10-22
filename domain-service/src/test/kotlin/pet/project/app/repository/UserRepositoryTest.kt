package pet.project.app.repository

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.user.UserNotificationDetails
import pet.project.app.model.domain.DomainUser
import pet.project.internal.commonmodels.user.user.User
import pet.project.internal.input.reqreply.user.create.CreateUserRequest
import pet.project.internal.input.reqreply.user.update.UpdateUserRequest
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

    private val firstUser = User.newBuilder().setLogin("firstUser").setEmail("first@mail.com").build()
    private val secondUser = User.newBuilder().setLogin("secondUser").setEmail("second@mail.com").build()
    private val firstCreateUserRequest = CreateUserRequest.newBuilder().setUser(firstUser).build()
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
                assertEquals(firstCreateUserRequest.user.login, savedUser.login)
                assertEquals(firstCreateUserRequest.user.email, savedUser.email)
                assertEquals(firstCreateUserRequest.user.bookWishListList.toSet(), savedUser.bookWishList)
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

        val savedUser = userRepository.insert(firstCreateUserRequest).block()!!
        val updateRequest = UpdateUserRequest.newBuilder().setLogin("updated_login").build()
        val expectedUpdatedUser = DomainUser(savedUser.id, "updated_login", savedUser.email, savedUser.bookWishList)

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
        val updateRequest = UpdateUserRequest.newBuilder().setLogin("login").setEmail("email@example.com").build()

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
        val firstUserCopy = firstUser.toBuilder().addAllBookWishList(setOf(firstBook.id, secondBook.id)).build()
        userRepository.insert(CreateUserRequest.newBuilder().setUser(firstUserCopy).build()).block()!!
        val secondUserCopy = secondUser.toBuilder().addAllBookWishList(setOf(thirdBook.id, firstBook.id, secondBook.id))
            .build()
        userRepository.insert(CreateUserRequest.newBuilder().setUser(secondUserCopy).build()).block()!!

        val requestIds = listOf(secondBook.id, thirdBook.id)
        val firstExpected = UserNotificationDetails(firstUserCopy.login, firstUserCopy.email, setOf(secondBook.title))
        val secondExpected = UserNotificationDetails(
            secondUser.login,
            secondUser.email,
            setOf(secondBook.title, thirdBook.title)
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
        val firstUserCopy = firstUser.toBuilder().addAllBookWishList(setOf(firstBook.id))
        val secondUserCopy = secondUser.toBuilder().addAllBookWishList(setOf(secondBook.id))

        userRepository.insert(CreateUserRequest.newBuilder().setUser(firstUserCopy).build()).block()!!
        userRepository.insert(CreateUserRequest.newBuilder().setUser(secondUserCopy).build()).block()!!

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
