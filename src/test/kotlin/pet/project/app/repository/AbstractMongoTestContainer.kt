package pet.project.app.repository

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.MongoDBContainer

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = [AbstractMongoTestContainer.Initializer::class])
interface AbstractMongoTestContainer {

    companion object {
        val mongoContainer = MongoDBContainer("mongo:7.0.14")
            .apply { start() }
    }

    class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            mongoContainer.start()

            TestPropertyValues.of(
                "spring.data.mongodb.uri=${mongoContainer.replicaSetUrl}"
            ).applyTo(configurableApplicationContext.environment)
        }
    }
}
