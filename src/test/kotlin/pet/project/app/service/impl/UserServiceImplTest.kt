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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import pet.project.app.dto.user.CreateUserRequest
import pet.project.app.dto.user.UpdateUserRequest
import pet.project.app.exception.BookNotFoundException
import pet.project.app.exception.UserNotFoundException
import pet.project.app.model.domain.DomainUser
import pet.project.app.repository.BookRepository
import pet.project.app.repository.UserRepository

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
    fun `check creates user`() {
        // GIVEN
        val userId = ObjectId.get().toHexString()
        val expectedUser = DomainUser(userId, "testUser123", "test.user@test.com", dummyStringWishList)
        val testRequest = CreateUserRequest("testUser123", "test.user@test.com", dummyStringWishList)
        every { userRepositoryMock.insert(testRequest) } returns expectedUser

        // WHEN
        val actualUser = userService.create(testRequest)

        // THEN
        verify { userRepositoryMock.insert(testRequest) }
        assertEquals(expectedUser, actualUser)
    }

    @Test
    fun `check getting user`() {
        // GIVEN
        val userId = "66c35b050da7b9523070cb3a"
        val expected = DomainUser(userId, "testUser123", "test.user@example.com", dummyStringWishList)
        every { userRepositoryMock.findById(userId) } returns expected

        // WHEN
        val actual = userService.getById(userId)

        // THEN
        verify { userRepositoryMock.findById(userId) }
        assertEquals(expected, actual)
    }

    @Test
    fun `check getting user by id throws exception when not found`() {
        // GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        every { userRepositoryMock.findById(testRequestUserId) } returns null

        // WHEN
        assertThrows<UserNotFoundException> {
            userService.getById(testRequestUserId)
        }

        // THEN
        verify { userRepositoryMock.findById(testRequestUserId) }
    }

    @Test
    fun `check updating user`() {
        // GIVEN
        val userId = "66c35b050da7b9523070cb3a"
        val updateUserRequest = UpdateUserRequest("John Doe", "test.user@example.com", dummyStringWishList)
        val updatedUser = DomainUser(userId, "John Doe", "test.user@example.com", dummyStringWishList)
        every { userRepositoryMock.update(userId, updateUserRequest) } returns updatedUser

        // WHEN
        val result = userService.update(userId, updateUserRequest)

        // THEN
        assertEquals(updatedUser, result)
        verify { userRepositoryMock.update(userId, updateUserRequest) }
    }

    @Test
    fun `addBookToWishList should throw UserNotFoundException if matchCount not equal 1`() {
        // GIVEN
        val userId = "nonexistentUserId"
        val bookId = "60f1b13e8f1b2c000b355777"

        every { bookRepositoryMock.existsById(bookId) } returns true
        every { userRepositoryMock.addBookToWishList(userId, bookId) } returns 0L

        // WHEN
        val exception = assertThrows<UserNotFoundException> {
            userService.addBookToWishList(userId, bookId)
        }

        // THEN
        assertEquals(
            "User with id=$userId was not found during adding book with id=$bookId into user wishlist",
            exception.message,
        )
    }

    @Test
    fun `addBookToWishList should throw BookNotFoundException if book does not exist`() {
        // GIVEN
        val userId = "validUserId"
        val bookId = "nonexistentBookId"

        every { bookRepositoryMock.existsById(bookId) } returns false

        // WHEN
        val exception = assertThrows<BookNotFoundException> {
            userService.addBookToWishList(userId, bookId)
        }

        // THEN
        assertEquals(
            "Book with id=$bookId was not found during adding book to wishlist of user with id=$userId",
            exception.message
        )

        verify { bookRepositoryMock.existsById(bookId) }
        verify(exactly = 0) {
            userRepositoryMock.addBookToWishList(
                any(),
                any()
            )
        }
    }

    @Test
    fun `check adding book to wishlist`() {
        // GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        val testRequestBookId = "66c3637847ff4c2f0242073e"
        every { bookRepositoryMock.existsById(testRequestBookId) } returns true
        every { userRepositoryMock.addBookToWishList(testRequestUserId, testRequestBookId) } returns 1L

        // WHEN
        val result = userService.addBookToWishList(testRequestUserId, testRequestBookId)

        // THEN
        assertTrue(result)
        verify { bookRepositoryMock.existsById(testRequestBookId) }
        verify { userRepositoryMock.addBookToWishList(testRequestUserId, testRequestBookId) }
    }

    @Test
    fun `check adding book to wishlist throws exception when book not found`() {
        // GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        val testRequestBookId = "66c3637847ff4c2f0242073e"

        every { bookRepositoryMock.existsById(testRequestBookId) } returns false

        // WHEN
        assertThrows<BookNotFoundException> {
            userService.addBookToWishList(testRequestUserId, testRequestBookId)
        }

        // THEN
        verify { bookRepositoryMock.existsById(testRequestBookId) }
    }

    @Test
    fun `check delete user`() {
        // GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        every { userRepositoryMock.delete(testRequestUserId) } returns 1L

        // WHEN
        userService.delete(testRequestUserId)

        // THEN
        verify { userRepositoryMock.delete(testRequestUserId) }
    }

    @Test
    fun `check delete logs warn when user is absent`() {
        // GIVEN
        val logger: Logger = LoggerFactory.getLogger(UserServiceImpl::class.java) as Logger
        val listAppender = ListAppender<ILoggingEvent>().apply { start() }
        logger.addAppender(listAppender)

        val userId = "66c35b050da7b9523070cb3a"
        every { userRepositoryMock.delete(userId) } returns 0L

        //WHEN
        userService.delete(userId)

        // THEN
        val logs = listAppender.list
        val expectedMessage = "Affected 0 documents while trying to delete user with id=$userId"
        assertEquals(expectedMessage, logs.first().formattedMessage)
        assertEquals(Level.WARN, logs.first().level)
    }
}
