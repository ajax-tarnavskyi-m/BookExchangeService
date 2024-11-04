package pet.project.app.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.IntegerDeserializer
import org.apache.kafka.common.serialization.IntegerSerializer
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

    @Bean
    fun kafkaReceiver(): KafkaReceiver<Int, String> {
        val config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to CONSUMER_GROUP_ID,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to IntegerDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        )
        val options = ReceiverOptions.create<Int, String>(config).subscription(setOf(topic))
        return KafkaReceiver.create(options)
    }

    @Bean
    fun kafkaSender(): KafkaSender<Int, String> {
        val config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.CLIENT_ID_CONFIG to PROVIDER_CLIENT_ID,
            ProducerConfig.ACKS_CONFIG to PROVIDER_ACKS,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to IntegerSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java
        )
        val senderOptions = SenderOptions.create<Int, String>(config)
        return KafkaSender.create(senderOptions)
    }

    companion object {
        private const val CONSUMER_GROUP_ID = "amount-increased-group"
        private const val PROVIDER_CLIENT_ID = "sample-producer"
        private const val PROVIDER_ACKS = "all"
    }
}
