package pet.project.app.repository

import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.user.UserNotificationDetails
import pet.project.app.model.domain.DomainUser
import pet.project.core.RandomTestFields.Book.randomBookIdString
import pet.project.core.RandomTestFields.Book.randomDescription
import pet.project.core.RandomTestFields.Book.randomPrice
import pet.project.core.RandomTestFields.Book.randomTitle
import pet.project.core.RandomTestFields.Book.randomYearOfPublishing
import pet.project.core.RandomTestFields.User.randomEmail
import pet.project.core.RandomTestFields.User.randomLogin
import pet.project.core.RandomTestFields.User.randomUserIdString
import pet.project.internal.commonmodels.user.User
import pet.project.internal.input.reqreply.user.CreateUserRequest
import pet.project.internal.input.reqreply.user.UpdateUserRequest
import reactor.kotlin.test.test
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("test")
class UserRepositoryTest {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var bookRepository: BookRepository

    private val firstUser = User.newBuilder().setLogin(randomLogin()).setEmail(randomEmail()).build()
    private val secondUser = User.newBuilder().setLogin(randomLogin()).setEmail(randomEmail()).build()
    private val createUserRequest = CreateUserRequest.newBuilder().setLogin(randomLogin()).setEmail(randomEmail())
        .build()
    private val firstCreateBookRequest =
        CreateBookRequest(randomTitle(), randomDescription(), randomYearOfPublishing(), randomPrice(), 0)
    private val secondCreateBookRequest =
        CreateBookRequest(randomTitle(), randomDescription(), randomYearOfPublishing(), randomPrice(), 0)
    private val thirdCreateBookRequest =
        CreateBookRequest(randomTitle(), randomDescription(), randomYearOfPublishing(), randomPrice(), 0)

    @Test
    fun `should save user and assign id`() {
        // WHEN
        val actualMono = userRepository.insert(createUserRequest)

        // THEN
        actualMono.test()
            .consumeNextWith { savedUser ->
                assertNotNull(savedUser.id, "Id should not be null after save")
                assertEquals(createUserRequest.login, savedUser.login)
                assertEquals(createUserRequest.email, savedUser.email)
                assertEquals(createUserRequest.bookWishListList.toSet(), savedUser.bookWishList)
            }
            .verifyComplete()
    }

    @Test
    fun `should return saved user by id`() {
        // GIVEN
        val savedUser = userRepository.insert(createUserRequest).block()!!

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
        val nonExistingId = randomUserIdString()
        val actualMono = userRepository.findById(nonExistingId)

        // THEN
        actualMono.test().verifyComplete()
    }

    @Test
    fun `should update user fields successfully`() {
        // GIVEN

        val savedUser = userRepository.insert(createUserRequest).block()!!
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
        val updateRequest = UpdateUserRequest.newBuilder().setLogin(randomLogin()).setEmail(randomEmail()).build()

        // WHEN
        val actualMono = userRepository.update(nonExistentUserId, updateRequest)

        // THEN
        actualMono.test().verifyComplete()
    }

    @Test
    fun `should add book id to user's wishlist`() {
        // GIVEN
        val savedUser = userRepository.insert(createUserRequest).block()!!
        val newBookIdString = randomBookIdString()
        val expectedModifiedCount = 1L

        // WHEN
        val actualMono = userRepository.addBookToWishList(savedUser.id, newBookIdString)

        // THEN
        actualMono.test()
            .expectNext(expectedModifiedCount)
            .verifyComplete()

        val updatedUser = userRepository.findById(savedUser.id).block()!!
        assertNotNull(updatedUser, "Updated user should be found")
        assertTrue(
            updatedUser.bookWishList.contains(newBookIdString),
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
        val actualFlux = userRepository.findAllSubscribersOf(listOf(randomBookIdString()))

        // THEN
        actualFlux.test().verifyComplete()
    }

    @Test
    fun `should remove user by id`() {
        // GIVEN
        val savedUser = userRepository.insert(createUserRequest).block()!!
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
