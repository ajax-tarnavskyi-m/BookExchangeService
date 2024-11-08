package pet.project.app.kafka

import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import pet.project.internal.app.topic.KafkaTopic
import pet.project.internal.input.pubsub.user_notification_details.BookAmountIncreasedEvent
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import reactor.kotlin.core.publisher.toFlux

@Component
class BookAmountIncreasedProducer(private val sender: KafkaSender<String, ByteArray>) {

    fun sendMessages(bookIds: List<String>) {
        sender.send(bookIds.toFlux().map { toSenderRecord(it) })
            .doOnError { e -> log.error("Send failed with book id $bookIds", e) }
            .subscribe()
    }

    private fun toSenderRecord(it: String): SenderRecord<String, ByteArray, String> {
        val event = BookAmountIncreasedEvent.newBuilder().setBookId(it).build()
        return SenderRecord.create(ProducerRecord(KafkaTopic.Book.AMOUNT_INCREASED, event.toByteArray()), null)
    }

    companion object {
        private val log = LoggerFactory.getLogger(BookAmountIncreasedProducer::class.java)
    }
}
