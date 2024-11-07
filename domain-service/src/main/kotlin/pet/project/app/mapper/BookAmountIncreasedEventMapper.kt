package pet.project.app.mapper

import org.apache.kafka.clients.producer.ProducerRecord
import pet.project.internal.input.pubsub.user_notification_details.BookAmountIncreasedEvent
import reactor.kafka.sender.SenderRecord

object BookAmountIncreasedEventMapper {
    fun ByteArray.toProto(): BookAmountIncreasedEvent {
        return BookAmountIncreasedEvent.parser().parseFrom(this)
    }

    fun toRecord(bookId: String, topic: String): SenderRecord<String, ByteArray, String> {
        return BookAmountIncreasedEvent.newBuilder().setBookId(bookId).build()
            .let { SenderRecord.create(ProducerRecord(topic, it.toByteArray()), null) }
    }
}
