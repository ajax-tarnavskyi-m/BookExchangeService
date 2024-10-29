package pet.project.app.service.impl

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import pet.project.app.model.domain.DomainUser
import pet.project.app.repository.BookRepository
import pet.project.app.repository.UserRepository
import pet.project.core.RandomTestFields.Book.randomBookIdString
import pet.project.core.RandomTestFields.User.randomEmail
import pet.project.core.RandomTestFields.User.randomLogin
import pet.project.core.RandomTestFields.User.randomUserIdString
import pet.project.core.exception.BookNotFoundException
import pet.project.core.exception.UserNotFoundException
import pet.project.internal.input.reqreply.user.CreateUserRequest
import pet.project.internal.input.reqreply.user.UpdateUserRequest
import pet.project.internal.input.reqreply.user.UpdateUserRequest.WishListUpdate
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import reactor.kotlin.test.verifyError

@ExtendWith(MockKExtension::class)
class UserServiceImplTest {

    @MockK
    lateinit var userRepositoryMock: UserRepository

    @MockK
    lateinit var bookRepositoryMock: BookRepository

    @InjectMockKs
    lateinit var userService: UserServiceImpl

    private val exampleUser =
        DomainUser(randomUserIdString(), randomLogin(), randomEmail(), setOf(randomBookIdString()))

    @Test
    fun `should create user successfully`() {
        // GIVEN
        val testRequest = CreateUserRequest.newBuilder()
            .setLogin(exampleUser.login)
            .setEmail(exampleUser.email)
            .addAllBookWishList(exampleUser.bookWishList)
            .build()
        every { userRepositoryMock.insert(testRequest) } returns exampleUser.toMono()

        // WHEN
        val actualMono = userService.create(testRequest)

        // THEN
        actualMono.test()
            .expectNext(exampleUser)
            .verifyComplete()

        verify { userRepositoryMock.insert(testRequest) }
    }

    @Test
    fun `should retrieve user by id`() {
        // GIVEN
        every { userRepositoryMock.findById(exampleUser.id) } returns exampleUser.toMono()

        // WHEN
        val actualMono = userService.getById(exampleUser.id)

        // THEN
        actualMono.test()
            .expectNext(exampleUser)
            .verifyComplete()

        verify { userRepositoryMock.findById(exampleUser.id) }
    }

    @Test
    fun `should return exception when user not found by id`() {
        // GIVEN
        val notExistingId = randomUserIdString()
        every { userRepositoryMock.findById(notExistingId) } returns Mono.empty()

        // WHEN
        val actualTest = userService.getById(notExistingId)

        // THEN
        actualTest.test().verifyError<UserNotFoundException>()
        verify { userRepositoryMock.findById(notExistingId) }
    }

    @Test
    fun `should update user successfully`() {
        // GIVEN
        val updateUserRequest = UpdateUserRequest.newBuilder()
            .setLogin(exampleUser.login)
            .setEmail(exampleUser.email)
            .setBookWishList(WishListUpdate.newBuilder().addAllBookIds(exampleUser.bookWishList))
            .build()

        every { userRepositoryMock.update(exampleUser.id, updateUserRequest) } returns exampleUser.toMono()

        // WHEN
        val actualMono = userService.update(exampleUser.id, updateUserRequest)

        // THEN
        actualMono.test()
            .expectNext(exampleUser)
            .verifyComplete()

        verify { userRepositoryMock.update(exampleUser.id, updateUserRequest) }
    }

    @Test
    fun `should throw UserNotFoundException when user not found while adding book to wishlist`() {
        // GIVEN
        val notExistingUserId = randomUserIdString()
        val bookIdString = randomBookIdString()
        every { bookRepositoryMock.existsById(bookIdString) } returns true.toMono()
        every { userRepositoryMock.addBookToWishList(notExistingUserId, bookIdString) } returns 0L.toMono()

        // WHEN
        val actualMono = userService.addBookToWishList(notExistingUserId, bookIdString)

        // THEN
        actualMono.test()
            .consumeErrorWith { ex ->
                assertEquals(UserNotFoundException::class.java, ex.javaClass)
                assertEquals("Could not find user($notExistingUserId) for wishlist update", ex.message)
            }.verify()

        verify { bookRepositoryMock.existsById(bookIdString) }
        verify { userRepositoryMock.addBookToWishList(notExistingUserId, bookIdString) }
    }

    @Test
    fun `should throw BookNotFoundException when book does not exist while adding to wishlist`() {
        // GIVEN
        val nonExistingBookId = "nonexistentBookId"

        every { bookRepositoryMock.existsById(nonExistingBookId) } returns false.toMono()

        // WHEN
        val actualMono = userService.addBookToWishList(exampleUser.id, nonExistingBookId)

        // THEN
        actualMono.test()
            .consumeErrorWith { ex ->
                assertEquals(BookNotFoundException::class.java, ex.javaClass)
                assertEquals("Could not find book($nonExistingBookId) for wishlist update", ex.message)
            }
            .verify()

        verify { bookRepositoryMock.existsById(nonExistingBookId) }
        verify(exactly = 0) { userRepositoryMock.addBookToWishList(any(), any()) }
    }

    @Test
    fun `should add book to user's wishlist successfully`() {
        // GIVEN
        val userIdString = randomUserIdString()
        val bookIdString = randomBookIdString()
        every { bookRepositoryMock.existsById(bookIdString) } returns true.toMono()
        every { userRepositoryMock.addBookToWishList(userIdString, bookIdString) } returns 1L.toMono()

        // WHEN
        val actualMono = userService.addBookToWishList(userIdString, bookIdString)

        // THEN
        actualMono.test()
            .expectNext(Unit)
            .verifyComplete()

        verify { bookRepositoryMock.existsById(bookIdString) }
        verify { userRepositoryMock.addBookToWishList(userIdString, bookIdString) }
    }

    @Test
    fun `should throw BookNotFoundException when adding book to wishlist if book is not found`() {
        // GIVEN
        val nonExistingBookId = randomBookIdString()
        every { bookRepositoryMock.existsById(nonExistingBookId) } returns false.toMono()

        // WHEN
        val actualMono = userService.addBookToWishList(exampleUser.id, nonExistingBookId)

        // THEN
        actualMono.test().verifyError<BookNotFoundException>()
        verify { bookRepositoryMock.existsById(nonExistingBookId) }
    }

    @Test
    fun `should delete user successfully`() {
        // GIVEN
        every { userRepositoryMock.delete(exampleUser.id) } returns 1L.toMono()

        // WHEN
        val actual = userService.delete(exampleUser.id)

        // THEN
        actual.test()
            .expectNext(Unit)
            .verifyComplete()

        verify { userRepositoryMock.delete(exampleUser.id) }
    }

    @Test
    fun `should log warning when trying to delete non-existent user`() {
        // GIVEN
        val logger: Logger = LoggerFactory.getLogger(UserServiceImpl::class.java) as Logger
        val listAppender = ListAppender<ILoggingEvent>().apply { start() }
        logger.addAppender(listAppender)

        val nonExistingUserId = randomUserIdString()
        every { userRepositoryMock.delete(nonExistingUserId) } returns 0L.toMono()

        // WHEN
        val actualMono = userService.delete(nonExistingUserId)

        // THEN
        actualMono.test()
            .expectNext(Unit)
            .verifyComplete()

        val logs = listAppender.list
        val expectedMessage = "Affected 0 documents while trying to delete user with id=$nonExistingUserId"
        assertEquals(expectedMessage, logs.first().formattedMessage)
        assertEquals(Level.WARN, logs.first().level)
    }
}
