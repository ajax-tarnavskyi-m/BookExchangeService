package pet.project.app.service.impl

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import pet.project.app.dto.user.UserNotificationDetails
import pet.project.app.repository.BookRepository
import pet.project.app.repository.UserRepository
import java.util.concurrent.TimeUnit

@ExtendWith(MockKExtension::class)
class NotificationServiceImplTest {
    @MockK
    private lateinit var bookRepository: BookRepository

    @MockK
    private lateinit var userRepository: UserRepository

    @InjectMockKs
    private lateinit var notificationService: NotificationServiceImpl

    @Test
    fun `notifySubscribedUsers should notify users when book is available`() {
        // GIVEN
        val bookId = "66faafae1d7c375ad3c2ab2d"
        val userDetailsList = listOf(
            UserNotificationDetails("user1", "user1@example.com", setOf("Book Title 1")),
            UserNotificationDetails("user2", "user2@example.com", setOf("Book Title 2"))
        )

        every { bookRepository.setShouldBeNotified(bookId, false) } returns 1L
        every { userRepository.findAllBookSubscribers(bookId) } returns userDetailsList

        // WHEN
        notificationService.notifySubscribedUsers(bookId)

        // THEN
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            verify { userRepository.findAllBookSubscribers(bookId) }
            verify { bookRepository.setShouldBeNotified(bookId, false) }
        }
    }

    @Test
    fun `notifySubscribedUsers should return false when no notification is needed`() {
        // GIVEN
        val bookId = "66faafae1d7c375ad3c2ab2d"
        every { bookRepository.setShouldBeNotified(bookId, false) } returns 0L

        // WHEN
        notificationService.notifySubscribedUsers(bookId)

        // THEN
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            verify { bookRepository.setShouldBeNotified(bookId, false) }
        }
        verify(exactly = 0) { userRepository.findAllBookSubscribers(any()) }
    }

    @Test
    fun `notifySubscribedUsers with multiple books should notify users`() {
        // GIVEN
        val bookIds = listOf("66faafae1d7c375ad3c2ab2d", "66faaf5e1d7c375ad3c2ab2c")
        val userDetailsList = listOf(
            UserNotificationDetails("user1", "user1@example.com", setOf("Book Title 1")),
            UserNotificationDetails("user2", "user2@example.com", setOf("Book Title 2"))
        )

        every { bookRepository.setShouldBeNotified("66faafae1d7c375ad3c2ab2d", false) } returns 1L
        every { bookRepository.setShouldBeNotified("66faaf5e1d7c375ad3c2ab2c", false) } returns 1L
        every { userRepository.findAllBookListSubscribers(bookIds) } returns userDetailsList

        // WHEN
        notificationService.notifySubscribedUsers(bookIds)

        // THEN
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            verify { userRepository.findAllBookListSubscribers(bookIds) }
        }
        verify { bookRepository.setShouldBeNotified("66faafae1d7c375ad3c2ab2d", false) }
        verify { bookRepository.setShouldBeNotified("66faaf5e1d7c375ad3c2ab2c", false) }
    }

    @Test
    fun `notifySubscribedUsers should not notify users when no books require notification`() {
        // GIVEN
        val bookIds = listOf("66faafae1d7c375ad3c2ab2d", "66faaf5e1d7c375ad3c2ab2c")

        every { bookRepository.setShouldBeNotified("66faafae1d7c375ad3c2ab2d", false) } returns 0L
        every { bookRepository.setShouldBeNotified("66faaf5e1d7c375ad3c2ab2c", false) } returns 0L

        // WHEN
        notificationService.notifySubscribedUsers(bookIds)

        // THEN
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            verify(exactly = 0) { userRepository.findAllBookSubscribers(any()) }
            verify(exactly = 0) { userRepository.findAllBookListSubscribers(any()) }
        }
        verify { bookRepository.setShouldBeNotified("66faafae1d7c375ad3c2ab2d", false) }
        verify { bookRepository.setShouldBeNotified("66faaf5e1d7c375ad3c2ab2c", false) }
    }

    @Test
    fun `notifySubscribedUsers should notify users when only one book requires notification`() {
        // GIVEN
        val bookIds = listOf("66faafae1d7c375ad3c2ab2d", "66faaf5e1d7c375ad3c2ab2c")
        val userDetailsList = listOf(
            UserNotificationDetails("user1", "user1@example.com", setOf("Book Title 1"))
        )

        every { bookRepository.setShouldBeNotified("66faafae1d7c375ad3c2ab2d", false) } returns 1L
        every { bookRepository.setShouldBeNotified("66faaf5e1d7c375ad3c2ab2c", false) } returns 0L
        every { userRepository.findAllBookSubscribers("66faafae1d7c375ad3c2ab2d") } returns userDetailsList

        // WHEN
        notificationService.notifySubscribedUsers(bookIds)

        // THEN
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            verify { userRepository.findAllBookSubscribers("66faafae1d7c375ad3c2ab2d") }
        }
        verify { bookRepository.setShouldBeNotified("66faafae1d7c375ad3c2ab2d", false) }
        verify { bookRepository.setShouldBeNotified("66faaf5e1d7c375ad3c2ab2c", false) }
    }
}
