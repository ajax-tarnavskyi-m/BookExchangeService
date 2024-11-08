package pet.project.app.processor

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import pet.project.app.annotation.Profiling
import pet.project.app.dto.user.UserNotificationDetails
import pet.project.app.repository.BookRepository
import pet.project.app.repository.UserRepository
import pet.project.internal.input.pubsub.user_notification_details.BookAmountIncreasedEvent
import reactor.core.Disposable
import reactor.core.publisher.Mono
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOffset
import reactor.kafka.receiver.ReceiverRecord
import reactor.kotlin.core.publisher.toMono
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

    private var lastOffset: ReceiverOffset? = null

    @EventListener(ApplicationReadyEvent::class)
    fun subscribeToReceiver(): Disposable {
        return kafkaReceiver.receive()
            .bufferTimeout(notificationMaxAmount, notificationInterval)
            .doOnNext { recordList -> lastOffset = recordList.last().receiverOffset() }
            .map { records -> toBookIdsList(records).toSet() }
            .flatMap { uniqueBookIds -> processIds(uniqueBookIds) }
            .subscribe { lastOffset?.commit()?.block() }
    }

    private fun toBookIdsList(records: List<ReceiverRecord<String, ByteArray>>): List<String> {
        return records.map { BookAmountIncreasedEvent.parseFrom(it.value()).bookId }
    }

    private fun processIds(uniqueBookIds: Set<String>): Mono<Unit> {
        return bookRepository.getShouldBeNotifiedBooks(uniqueBookIds)
            .map { books -> books.map { it.id!!.toHexString() } }
            .flatMapMany(userRepository::findAllSubscribersOf)
            .doOnNext { userDetails -> notifyUsers(userDetails) }
            .then(Unit.toMono())
    }

    private fun notifyUsers(userDetails: UserNotificationDetails) {
        log.info("Hi {}! There is new books for you: {}", userDetails.login, userDetails.bookTitles)
    }

    companion object {
        private val log = LoggerFactory.getLogger(NotificationProcessor::class.java)
    }
}
