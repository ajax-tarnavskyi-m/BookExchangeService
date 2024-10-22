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
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import pet.project.app.model.domain.DomainUser
import pet.project.app.repository.BookRepository
import pet.project.app.repository.UserRepository
import pet.project.core.exception.BookNotFoundException
import pet.project.core.exception.UserNotFoundException
import pet.project.internal.commonmodels.user.user.User
import pet.project.internal.input.reqreply.user.create.CreateUserRequest
import pet.project.internal.input.reqreply.user.update.UpdateUserRequest
import pet.project.internal.input.reqreply.user.update.UpdateUserRequest.WishListUpdate
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

    private val dummyStringWishList =
        setOf("66bf6bf8039339103054e21a", "66c3636647ff4c2f0242073d", "66c3637847ff4c2f0242073e")

    @Test
    fun `should create user successfully`() {
        // GIVEN
        val userId = ObjectId.get().toHexString()
        val expectedUser = DomainUser(userId, "testUser123", "test.user@test.com", dummyStringWishList)
        val user = User.newBuilder().setLogin("testUser123").setEmail("test.user@test.com").addAllBookWishList(dummyStringWishList).build()
        val testRequest = CreateUserRequest.newBuilder().setUser(user).build()
        every { userRepositoryMock.insert(testRequest) } returns expectedUser.toMono()

        // WHEN
        val actualMono = userService.create(testRequest)

        // THEN
        actualMono.test()
            .expectNext(expectedUser)
            .verifyComplete()

        verify { userRepositoryMock.insert(testRequest) }
    }

    @Test
    fun `should retrieve user by id`() {
        // GIVEN
        val userId = "66c35b050da7b9523070cb3a"
        val expected = DomainUser(userId, "testUser123", "test.user@example.com", dummyStringWishList)
        every { userRepositoryMock.findById(userId) } returns expected.toMono()

        // WHEN
        val actualMono = userService.getById(userId)

        // THEN
        actualMono.test()
            .expectNext(expected)
            .verifyComplete()

        verify { userRepositoryMock.findById(userId) }
    }

    @Test
    fun `should return exception when user not found by id`() {
        // GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        every { userRepositoryMock.findById(testRequestUserId) } returns Mono.empty()

        // WHEN
        val actualTest = userService.getById(testRequestUserId)

        // THEN
        actualTest.test().verifyError<UserNotFoundException>()

        verify { userRepositoryMock.findById(testRequestUserId) }
    }

    @Test
    fun `should update user successfully`() {
        // GIVEN
        val userId = "66c35b050da7b9523070cb3a"
        val updateUserRequest = UpdateUserRequest.newBuilder()
            .setLogin("John Doe")
            .setEmail("test.user@example.com")
            .setBookWishList(WishListUpdate.newBuilder().addAllBookIds(dummyStringWishList))
            .build()

        val updatedUser = DomainUser(userId, "John Doe", "test.user@example.com", dummyStringWishList)
        every { userRepositoryMock.update(userId, updateUserRequest) } returns updatedUser.toMono()

        // WHEN
        val actualMono = userService.update(userId, updateUserRequest)

        // THEN
        actualMono.test()
            .expectNext(updatedUser)
            .verifyComplete()

        verify { userRepositoryMock.update(userId, updateUserRequest) }
    }

    @Test
    fun `should throw UserNotFoundException when user not found while adding book to wishlist`() {
        // GIVEN
        val userId = "nonexistentUserId1234567"
        val bookId = "60f1b13e8f1b2c000b355777"

        every { bookRepositoryMock.existsById(bookId) } returns true.toMono()
        every { userRepositoryMock.addBookToWishList(userId, bookId) } returns 0L.toMono()

        // WHEN
        val actualMono = userService.addBookToWishList(userId, bookId)

        // THEN
        actualMono.test()
            .consumeErrorWith { ex ->
                assertEquals(UserNotFoundException::class.java, ex.javaClass)
                assertEquals("Could not find user($userId) for wishlist update", ex.message)
            }
            .verify()

        verify { bookRepositoryMock.existsById(bookId) }
        verify { userRepositoryMock.addBookToWishList(userId, bookId) }
    }

    @Test
    fun `should throw BookNotFoundException when book does not exist while adding to wishlist`() {
        // GIVEN
        val userId = "validUserId"
        val bookId = "nonexistentBookId"

        every { bookRepositoryMock.existsById(bookId) } returns false.toMono()

        // WHEN
        val actualMono = userService.addBookToWishList(userId, bookId)

        // THEN
        actualMono.test()
            .consumeErrorWith { ex ->
                assertEquals(BookNotFoundException::class.java, ex.javaClass)
                assertEquals("Could not find book($bookId) for wishlist update", ex.message)
            }
            .verify()

        verify { bookRepositoryMock.existsById(bookId) }
        verify(exactly = 0) { userRepositoryMock.addBookToWishList(any(), any()) }
    }

    @Test
    fun `should add book to user's wishlist successfully`() {
        // GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        val testRequestBookId = "66c3637847ff4c2f0242073e"
        every { bookRepositoryMock.existsById(testRequestBookId) } returns true.toMono()
        every { userRepositoryMock.addBookToWishList(testRequestUserId, testRequestBookId) } returns 1L.toMono()

        // WHEN
        val actualMono = userService.addBookToWishList(testRequestUserId, testRequestBookId)

        // THEN
        actualMono.test()
            .expectNext(Unit)
            .verifyComplete()

        verify { bookRepositoryMock.existsById(testRequestBookId) }
        verify { userRepositoryMock.addBookToWishList(testRequestUserId, testRequestBookId) }
    }

    @Test
    fun `should throw BookNotFoundException when adding book to wishlist if book is not found`() {
        // GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        val testRequestBookId = "66c3637847ff4c2f0242073e"

        every { bookRepositoryMock.existsById(testRequestBookId) } returns false.toMono()

        // WHEN
        val actualMono = userService.addBookToWishList(testRequestUserId, testRequestBookId)

        // THEN
        actualMono.test().verifyError<BookNotFoundException>()

        verify { bookRepositoryMock.existsById(testRequestBookId) }
    }

    @Test
    fun `should delete user successfully`() {
        // GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        every { userRepositoryMock.delete(testRequestUserId) } returns 1L.toMono()

        // WHEN
        val actual = userService.delete(testRequestUserId)

        // THEN
        actual.test()
            .expectNext(Unit)
            .verifyComplete()

        verify { userRepositoryMock.delete(testRequestUserId) }
    }

    @Test
    fun `should log warning when trying to delete non-existent user`() {
        // GIVEN
        val logger: Logger = LoggerFactory.getLogger(UserServiceImpl::class.java) as Logger
        val listAppender = ListAppender<ILoggingEvent>().apply { start() }
        logger.addAppender(listAppender)

        val userId = "66c35b050da7b9523070cb3a"
        every { userRepositoryMock.delete(userId) } returns 0L.toMono()

        // WHEN
        val actualMono = userService.delete(userId)

        // THEN
        actualMono.test()
            .expectNext(Unit)
            .verifyComplete()

        val logs = listAppender.list
        val expectedMessage = "Affected 0 documents while trying to delete user with id=$userId"
        assertEquals(expectedMessage, logs.first().formattedMessage)
        assertEquals(Level.WARN, logs.first().level)
    }
}
