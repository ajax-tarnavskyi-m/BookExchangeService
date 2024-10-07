package pet.project.app.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pet.project.app.annotation.Profiling
import pet.project.app.dto.user.UserNotificationDetails
import pet.project.app.repository.BookRepository
import pet.project.app.repository.UserRepository
import pet.project.app.service.NotificationService

@Profiling
@Service
class NotificationServiceImpl(
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
) : NotificationService {

    override fun notifySubscribedUsers(bookId: String) {
        if (updateShouldBeNotified(bookId)) {
            val userNotificationDetailsList = userRepository.findAllBookSubscribers(bookId)
            notifyUsers(userNotificationDetailsList)
        }
    }

    override fun notifySubscribedUsers(bookIds: List<String>) {
        val bookIdsForNotification = bookIds.filter { updateShouldBeNotified(it) }
        val userNotificationDetailsList = when(bookIdsForNotification.size) {
            0 -> return
            1 -> userRepository.findAllBookSubscribers(bookIdsForNotification.first())
            else -> userRepository.findAllBookListSubscribers(bookIdsForNotification)
        }
        notifyUsers(userNotificationDetailsList)
    }

    private fun notifyUsers(userNotificationDetailsList: List<UserNotificationDetails>) {
        for (userDetails in userNotificationDetailsList) {
            log.info("Hi {}! There is new books for you: {}", userDetails.login, userDetails.bookTitles)
        }
    }

    private fun updateShouldBeNotified(bookId: String) =
        bookRepository.updateShouldBeNotified(bookId, false) == 1L

    companion object {
        private val log = LoggerFactory.getLogger(NotificationServiceImpl::class.java)
    }
}
