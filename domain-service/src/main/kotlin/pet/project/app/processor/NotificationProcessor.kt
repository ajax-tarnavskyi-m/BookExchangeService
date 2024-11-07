package pet.project.app.processor

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import pet.project.app.annotation.Profiling
import pet.project.app.dto.user.UserNotificationDetails
import pet.project.app.mapper.BookAmountIncreasedEventMapper.toProto
import pet.project.app.repository.BookRepository
import pet.project.app.repository.UserRepository
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverRecord
import java.time.Duration

@Profiling
@Component
@Profile("!test")
class NotificationProcessor(
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val kafkaReceiver: KafkaReceiver<String, ByteArray>,
    @Value("\${notification.buffer.interval:PT5M}") private val notificationInterval: Duration,
    @Value("\${notification.buffer.items-max-amount:100}") private val notificationMaxAmount: Int,
) {

    @EventListener(ApplicationReadyEvent::class)
    fun subscribeToReceiver(): Disposable {
        return kafkaReceiver.receive()
            .bufferTimeout(notificationMaxAmount, notificationInterval)
            .doOnNext { recordList -> recordList.last().receiverOffset().acknowledge() }
            .flatMap { recordList -> findSubscribedUsersDetails(recordList) }
            .subscribe { userDetails -> notifyUsers(userDetails) }
    }

    private fun findSubscribedUsersDetails(
        records: List<ReceiverRecord<String, ByteArray>>,
    ): Flux<UserNotificationDetails> {
        val uniqueBookIds = records.map { it.value().toProto().bookId }.toSet()
        return Flux.fromIterable(uniqueBookIds)
            .filterWhen { updateShouldBeNotified(it) }
            .collectList()
            .filter { it.isNotEmpty() }
            .flatMapMany(userRepository::findAllSubscribersOf)
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
