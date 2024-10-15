package pet.project.app.processor

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import pet.project.app.annotation.Profiling
import pet.project.app.dto.user.UserNotificationDetails
import pet.project.app.repository.BookRepository
import pet.project.app.repository.UserRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.time.Duration

@Profiling
@Component
class NotificationProcessor(
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val availableBooksSink: Sinks.Many<String>,
    @Value("\${notification.buffer.interval:PT5M}") private val notificationInterval: String,
    @Value("\${notification.buffer.items-max-amount:100}") private val notificationMaxAmount: Int,
) {

    @PostConstruct
    fun subscribeToSink() {
        availableBooksSink.asFlux()
            .bufferTimeout(notificationMaxAmount, Duration.parse(notificationInterval))
            .flatMap { bookIdsList -> findSubscribedUsersDetails(bookIdsList) }
            .subscribe { userDetails -> notifyUsers(userDetails) }
    }

    private fun findSubscribedUsersDetails(bookIds: List<String>): Flux<UserNotificationDetails> {
        return Flux.fromIterable(bookIds)
            .filterWhen { updateShouldBeNotified(it) }
            .collectList()
            .flatMapMany { list -> userRepository.findAllSubscribersOf(list) }
    }

    private fun notifyUsers(userDetails: UserNotificationDetails) {
        log.info("Hi {}! There is new books for you: {}", userDetails.login, userDetails.bookTitles)
    }

    private fun updateShouldBeNotified(bookId: String): Mono<Boolean> {
        return bookRepository.updateShouldBeNotified(bookId, false)
            .map { it == 1L }
    }

    companion object {
        private val log = LoggerFactory.getLogger(NotificationProcessor::class.java)
    }
}
