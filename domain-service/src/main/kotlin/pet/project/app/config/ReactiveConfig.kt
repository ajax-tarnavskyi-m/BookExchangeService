package pet.project.app.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.ReactiveMongoTransactionManager
import org.springframework.data.mongodb.SessionSynchronization
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Sinks

@Configuration
class ReactiveConfig {

    @Bean
    fun availableBooksSink(): Sinks.Many<String> {
        return Sinks
            .many()
            .unicast()
            .onBackpressureBuffer()
    }

    @Bean
    fun reactiveTransactionManager(databaseFactory: ReactiveMongoDatabaseFactory): ReactiveTransactionManager {
        return ReactiveMongoTransactionManager(databaseFactory)
    }

    @Bean
    fun transactionalOperator(reactiveTransactionManager: ReactiveTransactionManager): TransactionalOperator {
        return TransactionalOperator.create(reactiveTransactionManager)
    }

    @Bean
    fun reactiveMongoTemplate(databaseFactory: ReactiveMongoDatabaseFactory): ReactiveMongoTemplate {
        return ReactiveMongoTemplate(databaseFactory)
            .apply { setSessionSynchronization(SessionSynchronization.ALWAYS) }
    }
}
