package pet.project.app.kafka

import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord

@Component
class BookAmountIncreasedProducer {
    @Autowired
    private lateinit var sender: KafkaSender<Int, String>

    @Value("\${spring.kafka.topic.amount-increased}")
    lateinit var topic: String

    fun sendMessages(bookIds: List<String>) {
        sender.send(Flux.fromIterable(bookIds).map(::toRecord))
            .doOnError { e -> log.error("Send failed with book id $bookIds", e) }
            .subscribe { log.info("SUCCESSFULLY SENT $bookIds") }
    }

    private fun toRecord(it: String): SenderRecord<Int, String, String> {
        return SenderRecord.create(ProducerRecord(topic, it), "metadata")
    }

    companion object {
        private val log = LoggerFactory.getLogger(BookAmountIncreasedProducer::class.java)
    }
}
