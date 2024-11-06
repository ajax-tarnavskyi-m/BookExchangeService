package pet.project.app.kafka

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import pet.project.app.mapper.BookAmountIncreasedEventMapper.toRecord
import reactor.core.publisher.Flux
import reactor.kafka.sender.KafkaSender

@Component
class BookAmountIncreasedProducer {
    @Autowired
    private lateinit var sender: KafkaSender<String, ByteArray>

    @Value("\${spring.kafka.topic.amount-increased}")
    lateinit var topic: String

    fun sendMessages(bookIds: List<String>) {
        sender.send(Flux.fromIterable(bookIds).map { bookId -> toRecord(bookId, topic) })
            .doOnError { e -> log.error("Send failed with book id $bookIds", e) }
            .subscribe()
    }

    companion object {
        private val log = LoggerFactory.getLogger(BookAmountIncreasedProducer::class.java)
    }
}
