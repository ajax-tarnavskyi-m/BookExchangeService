package pet.project.app.service

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import pet.project.app.exception.BookNotFoundException
import pet.project.app.exception.UserNotFoundException
import pet.project.app.model.User
import pet.project.app.repository.BookRepository
import pet.project.app.repository.UserRepository

class UserServiceTest {

    @MockK
    lateinit var userRepositoryMock: UserRepository


    @MockK
    lateinit var bookRepositoryMock: BookRepository

    @InjectMockKs
    lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    private val dummyWishlist = setOf(
        "66bf6bf8039339103054e21a", "66c3636647ff4c2f0242073d",
        "66c3637847ff4c2f0242073e"
    )

    @Test
    fun `check creates user`() {
        //GIVEN
        val inputUser = User(login = "testUser123", bookWishList = dummyWishlist)
        val expected = User(ObjectId("66c35b050da7b9523070cb3a"), "testUser123", dummyWishlist)
        every { userRepositoryMock.save(inputUser) } returns expected

        //WHEN
        val actual = userService.create(inputUser)

        //THEN
        verify { userRepositoryMock.save((inputUser)) }
        assertEquals(expected, actual)
    }

    @Test
    fun `check getting user`() {
        //GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        val expected = User(ObjectId("66c35b050da7b9523070cb3a"), "testUser123", dummyWishlist)
        every { userRepositoryMock.findByIdOrNull(testRequestUserId) } returns expected

        //WHEN
        val actual = userService.getById(testRequestUserId)

        //THEN
        verify { userRepositoryMock.findByIdOrNull(testRequestUserId) }
        assertEquals(expected, actual)
    }

    @Test
    fun `check getting user by id throws exception when not found`() {
        //GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        every { userRepositoryMock.findByIdOrNull(testRequestUserId) } returns null

        // THEN
        assertThrows(UserNotFoundException::class.java) {
            // WHEN
            userService.getById(testRequestUserId)
        }

        verify { userRepositoryMock.findByIdOrNull(testRequestUserId) }
    }

    @Test
    fun `check updating user`() {
        //GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        val user = User(ObjectId(testRequestUserId), "John Doe", dummyWishlist)

        every { userRepositoryMock.existsById(testRequestUserId)} returns true
        every { userRepositoryMock.save(user) } returns user

        // WHEN
        val result = userService.update(user)

        // THEN
        assertEquals(user, result)
        verify { userRepositoryMock.existsById(testRequestUserId) }
        verify { userRepositoryMock.save(user) }
    }

    @Test
    fun `check updating user throws exception when user not found`() {
        //GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        val user = User(ObjectId(testRequestUserId), "John Doe", dummyWishlist)

        every { userRepositoryMock.existsById(testRequestUserId) } returns false

        // THEN
        assertThrows(UserNotFoundException::class.java) {
            // WHEN
            userService.update(user)
        }

        verify { userRepositoryMock.existsById(testRequestUserId) }
    }

    @Test
    fun `check adding book to wishlist`() {
        //GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        val testRequestBookId = "66c3637847ff4c2f0242073e"
        val user = User(ObjectId(testRequestUserId), "John Doe", dummyWishlist)

        every { userRepositoryMock.findByIdOrNull(testRequestUserId) } returns user
        every { bookRepositoryMock.existsById(testRequestBookId) } returns true
        every { userRepositoryMock.save(any()) } returns user.copy(bookWishList = setOf(testRequestBookId))

        // WHEN
        val result = userService.addBookToWishList(testRequestUserId, testRequestBookId)

        // THEN
        assertTrue(result.bookWishList.contains(testRequestBookId))
        verify { userRepositoryMock.findByIdOrNull(testRequestUserId) }
        verify { bookRepositoryMock.existsById(testRequestBookId) }
        verify { userRepositoryMock.save(any()) }
    }

    @Test
    fun `check adding book to wishlist throws exception when book not found`() {
        //GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        val testRequestBookId = "66c3637847ff4c2f0242073e"
        val user = User(ObjectId(testRequestUserId), "John Doe", dummyWishlist)

        every { userRepositoryMock.findByIdOrNull(testRequestUserId) } returns user
        every { bookRepositoryMock.existsById(testRequestBookId) } returns false

        // THEN
        assertThrows(BookNotFoundException::class.java) {
            // WHEN
            userService.addBookToWishList(testRequestUserId, testRequestBookId)
        }

        verify { userRepositoryMock.findByIdOrNull(testRequestUserId) }
        verify { bookRepositoryMock.existsById(testRequestBookId) }
    }


    @Test
    fun `check delete user`() {
        //GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"
        every { userRepositoryMock.existsById(testRequestUserId) } returns true
        every { userRepositoryMock.deleteById(testRequestUserId) } just Runs

        //WHEN
        userService.delete(testRequestUserId)

        //THEN
        verify { userRepositoryMock.existsById(testRequestUserId) }
        verify { userRepositoryMock.deleteById(testRequestUserId) }
    }

    @Test
    fun `check deleting user when user does not exist`() {
        //GIVEN
        val testRequestUserId = "66c35b050da7b9523070cb3a"

        every { userRepositoryMock.existsById(testRequestUserId) } returns false

        // WHEN
        userService.delete(testRequestUserId)

        // THEN
        verify { userRepositoryMock.existsById(testRequestUserId) }
        verify(exactly = 0) { userRepositoryMock.deleteById(testRequestUserId) }
    }
}
