package pet.project.app.repository

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.user.UserNotificationDetails
import pet.project.app.model.domain.DomainUser
import pet.project.core.RandomTestFields.Book.description
import pet.project.core.RandomTestFields.Book.price
import pet.project.core.RandomTestFields.Book.title
import pet.project.core.RandomTestFields.Book.yearOfPublishing
import pet.project.core.RandomTestFields.SecondBook.secondDescription
import pet.project.core.RandomTestFields.SecondBook.secondPrice
import pet.project.core.RandomTestFields.SecondBook.secondTitle
import pet.project.core.RandomTestFields.SecondBook.secondYearOfPublishing
import pet.project.core.RandomTestFields.SecondUser.secondEmail
import pet.project.core.RandomTestFields.SecondUser.secondLogin
import pet.project.core.RandomTestFields.ThirdBook.thirdDescription
import pet.project.core.RandomTestFields.ThirdBook.thirdPrice
import pet.project.core.RandomTestFields.ThirdBook.thirdTitle
import pet.project.core.RandomTestFields.ThirdBook.thirdYearOfPublishing
import pet.project.core.RandomTestFields.User.email
import pet.project.core.RandomTestFields.User.login
import pet.project.internal.commonmodels.user.User
import pet.project.internal.input.reqreply.user.CreateUserRequest
import pet.project.internal.input.reqreply.user.UpdateUserRequest
import reactor.kotlin.test.test
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserRepositoryTest : AbstractTestContainer {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var bookRepository: BookRepository

    private val firstUser = User.newBuilder().setLogin(login).setEmail(email).build()
    private val secondUser = User.newBuilder().setLogin(secondLogin).setEmail(secondEmail).build()
    private val firstCreateUserRequest = CreateUserRequest.newBuilder().setLogin(login).setEmail(email).build()
    private val firstCreateBookRequest = CreateBookRequest(title, description, yearOfPublishing, price, 0)
    private val secondCreateBookRequest =
        CreateBookRequest(secondTitle, secondDescription, secondYearOfPublishing, secondPrice, 0)
    private val thirdCreateBookRequest =
        CreateBookRequest(thirdTitle, thirdDescription, thirdYearOfPublishing, thirdPrice, 0)

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
                assertEquals(firstCreateUserRequest.bookWishListList.toSet(), savedUser.bookWishList)
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
        val nonExistingId = ObjectId.get().toHexString()
        val actualMono = userRepository.findById(nonExistingId)

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
        val updateRequest = UpdateUserRequest.newBuilder().setLogin(login).setEmail(email).build()

        // WHEN
        val actualMono = userRepository.update(nonExistentUserId, updateRequest)

        // THEN
        actualMono.test().verifyComplete()
    }

    @Test
    fun `should add book id to user's wishlist`() {
        // GIVEN
        val savedUser = userRepository.insert(firstCreateUserRequest).block()!!
        val newBookId = ObjectId.get().toHexString()
        val expectedModifiedCount = 1L

        // WHEN
        val actualMono = userRepository.addBookToWishList(savedUser.id, newBookId)

        // THEN
        actualMono.test()
            .expectNext(expectedModifiedCount)
            .verifyComplete()

        val updatedUser = userRepository.findById(savedUser.id).block()!!
        assertNotNull(updatedUser, "Updated user should be found")
        assertTrue(
            updatedUser.bookWishList.contains(newBookId),
            "The book should be in the user's wish list"
        )
    }

    @Test
    fun `should return users with correct book titles in their wishlist`() {
        // GIVEN
        val firstBook = bookRepository.insert(firstCreateBookRequest).block()!!
        val secondBook = bookRepository.insert(secondCreateBookRequest).block()!!
        val thirdBook = bookRepository.insert(thirdCreateBookRequest).block()!!

        val firstUserCreationRequest = CreateUserRequest.newBuilder()
            .setLogin(firstUser.login)
            .setEmail(firstUser.email)
            .addAllBookWishList(setOf(firstBook.id, secondBook.id)).build()
        val secondCreateUserRequest = CreateUserRequest.newBuilder()
            .setLogin(secondUser.login)
            .setEmail(secondUser.email)
            .addAllBookWishList(setOf(thirdBook.id, firstBook.id, secondBook.id))
            .build()

        userRepository.insert(firstUserCreationRequest).block()!!
        userRepository.insert(secondCreateUserRequest).block()!!

        val firstExpected = UserNotificationDetails(firstUser.login, firstUser.email, setOf(secondBook.title))
        val secondExpected =
            UserNotificationDetails(secondUser.login, secondUser.email, setOf(secondBook.title, thirdBook.title))

        // WHEN
        val actualFlux = userRepository.findAllSubscribersOf(listOf(secondBook.id, thirdBook.id))

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

        val firstUserCreationRequest = CreateUserRequest.newBuilder()
            .setLogin(firstUser.login)
            .setEmail(firstUser.email)
            .addAllBookWishList(setOf(firstBook.id)).build()
        val secondCreateUserRequest = CreateUserRequest.newBuilder()
            .setLogin(secondUser.login)
            .setEmail(secondUser.email)
            .addAllBookWishList(setOf(secondBook.id))
            .build()

        userRepository.insert(firstUserCreationRequest).block()!!
        userRepository.insert(secondCreateUserRequest).block()!!

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
