package pet.project.app.mapper

import org.junit.jupiter.api.Assertions.assertEquals
import pet.project.app.mapper.UserThrowableMapper.toFailureAddBookToUserWishListResponse
import pet.project.app.mapper.UserThrowableMapper.toFailureCreateUserResponse
import pet.project.app.mapper.UserThrowableMapper.toFailureDeleteUserByIdResponse
import pet.project.app.mapper.UserThrowableMapper.toFailureFindUserByIdResponse
import pet.project.app.mapper.UserThrowableMapper.toFailureUpdateUserResponse
import pet.project.core.exception.BookNotFoundException
import pet.project.core.exception.UserNotFoundException
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserThrowableMapperTest {

    @Test
    fun `should map Throwable to CreateUserResponse with failure message`() {
        // GIVEN
        val errorMessage = "Test error occurred"
        val exception = RuntimeException(errorMessage)

        // WHEN
        val response = exception.toFailureCreateUserResponse()

        // THEN
        assertTrue(response.hasFailure(), "Response should contain failure")
        assertEquals(errorMessage, response.failure.message, "Failure message should match exception message")
    }

    @Test
    fun `should map UserNotFoundException to AddBookToUsersWishListResponse with user not found failure`() {
        // GIVEN
        val exception = UserNotFoundException("User not found")

        // WHEN
        val response = exception.toFailureAddBookToUserWishListResponse()

        // THEN
        assertTrue(response.hasFailure(), "Response should contain failure")
        assertEquals("User not found", response.failure.message)
        assertTrue(response.failure.hasUserNotFound(), "Response should contain user not found failure")
    }

    @Test
    fun `should map BookNotFoundException to AddBookToUsersWishListResponse with book not found failure`() {
        // GIVEN
        val exception = BookNotFoundException("Book not found")

        // WHEN
        val response = exception.toFailureAddBookToUserWishListResponse()

        // THEN
        assertTrue(response.hasFailure(), "Response should contain failure")
        assertEquals("Book not found", response.failure.message)
        assertTrue(response.failure.hasBookNotFound(), "Response should contain book not found failure")
    }

    @Test
    fun `should map generic exception to AddBookToUsersWishListResponse with failure message`() {
        // GIVEN
        val exception = RuntimeException("Generic error")

        // WHEN
        val response = exception.toFailureAddBookToUserWishListResponse()

        // THEN
        assertTrue(response.hasFailure(), "Response should contain failure")
        assertEquals("Generic error", response.failure.message)
        assertFalse(response.failure.hasUserNotFound(), "Response should not contain user not found failure")
        assertFalse(response.failure.hasBookNotFound(), "Response should not contain book not found failure")
    }

    @Test
    fun `should map UserNotFoundException to FindUserByIdResponse with user not found failure`() {
        // GIVEN
        val exception = UserNotFoundException("User not found")

        // WHEN
        val response = exception.toFailureFindUserByIdResponse()

        // THEN
        assertTrue(response.hasFailure(), "Response should contain failure")
        assertEquals("User not found", response.failure.message)
        assertTrue(response.failure.hasUserNotFound(), "Response should contain user not found failure")
    }

    @Test
    fun `should map generic exception to FindUserByIdResponse with failure message`() {
        // GIVEN
        val exception = RuntimeException("Generic error")

        // WHEN
        val response = exception.toFailureFindUserByIdResponse()

        // THEN
        assertTrue(response.hasFailure(), "Response should contain failure")
        assertEquals("Generic error", response.failure.message)
        assertFalse(response.failure.hasUserNotFound(), "Response should not contain user not found failure")
    }

    @Test
    fun `should map UserNotFoundException to UpdateUserResponse with user not found failure`() {
        // GIVEN
        val exception = UserNotFoundException("User not found")

        // WHEN
        val response = exception.toFailureUpdateUserResponse()

        // THEN
        assertTrue(response.hasFailure(), "Response should contain failure")
        assertEquals("User not found", response.failure.message)
        assertTrue(response.failure.hasUserNotFound(), "Response should contain user not found failure")
    }

    @Test
    fun `should map generic exception to UpdateUserResponse with failure message`() {
        // GIVEN
        val exception = RuntimeException("Generic error")

        // WHEN
        val response = exception.toFailureUpdateUserResponse()

        // THEN
        assertTrue(response.hasFailure(), "Response should contain failure")
        assertEquals("Generic error", response.failure.message)
        assertFalse(response.failure.hasUserNotFound(), "Response should not contain user not found failure")
    }

    @Test
    fun `should map exception to DeleteUserByIdResponse with failure message`() {
        // GIVEN
        val exception = RuntimeException("Delete operation failed")

        // WHEN
        val response = exception.toFailureDeleteUserByIdResponse()

        // THEN
        assertTrue(response.hasFailure(), "Response should contain failure")
        assertEquals("Delete operation failed", response.failure.message)
    }
}
