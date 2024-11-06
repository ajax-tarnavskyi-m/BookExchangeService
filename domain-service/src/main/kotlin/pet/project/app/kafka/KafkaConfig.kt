package pet.project.app.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions

@Configuration
class KafkaConfig {
    @Value("\${spring.kafka.bootstrap-servers}")
    lateinit var bootstrapServers: String

    @Value("\${spring.kafka.topic.amount-increased}")
    lateinit var topic: String

    @Value("\${spring.kafka.consumer.auto-offset-reset}")
    lateinit var autoOffsetReset: String

    @Bean
    fun kafkaReceiver(): KafkaReceiver<String, ByteArray> {
        val config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to CONSUMER_GROUP_ID,
            ConsumerConfig.CLIENT_ID_CONFIG to CONSUMER_CLIENT_ID,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to autoOffsetReset,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ByteArrayDeserializer::class.java,
        )
        val options = ReceiverOptions.create<String, ByteArray>(config).subscription(setOf(topic))
        return KafkaReceiver.create(options)
    }

    @Bean
    fun kafkaSender(): KafkaSender<String, ByteArray> {
        val config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.CLIENT_ID_CONFIG to PRODUCER_CLIENT_ID,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to ByteArraySerializer::class.java
        )
        val senderOptions = SenderOptions.create<String, ByteArray>(config)
        return KafkaSender.create(senderOptions)
    }

    companion object {
        private const val CONSUMER_GROUP_ID = "amount-increased-group"
        private const val CONSUMER_CLIENT_ID = "amount-increased-consumer"
        private const val PRODUCER_CLIENT_ID = "amount-increased-event-producer"
    }
}
