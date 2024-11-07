package pet.project.app.kafka

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import pet.project.app.mapper.BookAmountIncreasedEventMapper.toRecord
import pet.project.internal.app.topic.KafkaTopic
import reactor.core.publisher.Flux
import reactor.kafka.sender.KafkaSender

@Component
class BookAmountIncreasedProducer(private val sender: KafkaSender<String, ByteArray>) {

    fun sendMessages(bookIds: List<String>) {
        sender.send(Flux.fromIterable(bookIds).map { bookId -> toRecord(bookId, KafkaTopic.Book.AMOUNT_INCREASED) })
            .doOnError { e -> log.error("Send failed with book id $bookIds", e) }
            .subscribe()
    }

    companion object {
        private val log = LoggerFactory.getLogger(BookAmountIncreasedProducer::class.java)
    }
}
