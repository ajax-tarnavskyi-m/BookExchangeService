package pet.project.app.service.impl

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import pet.project.app.dto.user.UserNotificationDetails
import pet.project.app.repository.BookRepository
import pet.project.app.repository.UserRepository

@ExtendWith(MockKExtension::class)
class NotificationServiceImplTest {
    @MockK
    private lateinit var bookRepository: BookRepository

    @MockK
    private lateinit var userRepository: UserRepository

    @InjectMockKs
    private lateinit var notificationService: NotificationServiceImpl

    @Test
    fun `should notify users when book is available`() {
        // GIVEN
        val bookId = "66faafae1d7c375ad3c2ab2d"
        val userDetailsList = listOf(
            UserNotificationDetails("user1", "user1@example.com", setOf("Book Title 1")),
            UserNotificationDetails("user2", "user2@example.com", setOf("Book Title 2"))
        )

        every { bookRepository.updateShouldBeNotified(bookId, false) } returns 1L
        every { userRepository.findAllSubscribersOf(bookId) } returns userDetailsList

        // WHEN
        notificationService.notifySubscribedUsers(bookId)

        // THEN
        verify { userRepository.findAllSubscribersOf(bookId) }
        verify { bookRepository.updateShouldBeNotified(bookId, false) }
    }

    @Test
    fun `should not notify users when no notification is needed for the book`() {
        // GIVEN
        val bookId = "66faafae1d7c375ad3c2ab2d"
        every { bookRepository.updateShouldBeNotified(bookId, false) } returns 0L

        // WHEN
        notificationService.notifySubscribedUsers(bookId)

        // THEN
        verify { bookRepository.updateShouldBeNotified(bookId, false) }
        verify(exactly = 0) { userRepository.findAllSubscribersOf(any<String>()) }
    }

    @Test
    fun `should notify users when multiple books require notification`() {
        // GIVEN
        val bookIds = listOf("66faafae1d7c375ad3c2ab2d", "66faaf5e1d7c375ad3c2ab2c")
        val userDetailsList = listOf(
            UserNotificationDetails("user1", "user1@example.com", setOf("Book Title 1")),
            UserNotificationDetails("user2", "user2@example.com", setOf("Book Title 2"))
        )

        every { bookRepository.updateShouldBeNotified(bookIds[0], false) } returns 1L
        every { bookRepository.updateShouldBeNotified(bookIds[1], false) } returns 1L
        every { userRepository.findAllSubscribersOf(bookIds) } returns userDetailsList

        // WHEN
        notificationService.notifySubscribedUsers(bookIds)

        // THEN
        verify { userRepository.findAllSubscribersOf(bookIds) }
        verify { bookRepository.updateShouldBeNotified(bookIds[0], false) }
        verify { bookRepository.updateShouldBeNotified(bookIds[1], false) }
    }

    @Test
    fun `should not notify users when no books require notification`() {
        // GIVEN
        val bookIds = listOf("66faafae1d7c375ad3c2ab2d", "66faaf5e1d7c375ad3c2ab2c")

        every { bookRepository.updateShouldBeNotified(bookIds[0], false) } returns 0L
        every { bookRepository.updateShouldBeNotified(bookIds[1], false) } returns 0L

        // WHEN
        notificationService.notifySubscribedUsers(bookIds)

        // THEN
        verify(exactly = 0) { userRepository.findAllSubscribersOf(any<String>()) }
        verify(exactly = 0) { userRepository.findAllSubscribersOf(any<List<String>>()) }
        verify { bookRepository.updateShouldBeNotified(bookIds[0], false) }
        verify { bookRepository.updateShouldBeNotified(bookIds[1], false) }
    }

    @Test
    fun `should notify users when only one book requires notification`() {
        // GIVEN
        val bookIds = listOf("66faafae1d7c375ad3c2ab2d", "66faaf5e1d7c375ad3c2ab2c")
        val userDetailsList = listOf(
            UserNotificationDetails("user1", "user1@example.com", setOf("Book Title 1"))
        )

        every { bookRepository.updateShouldBeNotified(bookIds[0], false) } returns 1L
        every { bookRepository.updateShouldBeNotified(bookIds[1], false) } returns 0L
        every { userRepository.findAllSubscribersOf(bookIds[0]) } returns userDetailsList

        // WHEN
        notificationService.notifySubscribedUsers(bookIds)

        // THEN
        verify { userRepository.findAllSubscribersOf(bookIds[0]) }
        verify { bookRepository.updateShouldBeNotified(bookIds[0], false) }
        verify { bookRepository.updateShouldBeNotified(bookIds[1], false) }
    }
}
