package pet.project.app.config

import io.nats.client.Connection
import io.nats.client.Dispatcher
import io.nats.client.Nats
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class NatsConfig {
    @Bean
    fun natsConnection(): Connection = Nats.connect()

    @Bean
    fun createDispatcher(connection: Connection): Dispatcher {
        return connection.createDispatcher()
    }
}