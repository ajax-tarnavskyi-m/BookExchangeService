package pet.project.app.mapper

import org.apache.kafka.clients.producer.ProducerRecord
import pet.project.internal.input.pubsub.user_notification_details.BookAmountIncreasedEvent
import reactor.kafka.sender.SenderRecord

object BookAmountIncreasedEventMapper {
    fun ByteArray.toProto(): BookAmountIncreasedEvent {
        return BookAmountIncreasedEvent.parser().parseFrom(this)
    }

    fun toRecord(bookId: String, topic: String): SenderRecord<String, ByteArray, String> {
        val event = BookAmountIncreasedEvent.newBuilder().setBookId(bookId).build()
        return SenderRecord.create(ProducerRecord(topic, event.toByteArray()), null)
    }
}
