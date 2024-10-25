package pet.project.app.repository

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import pet.project.app.BookExchangeServiceApplication

@SpringBootTest(classes = [BookExchangeServiceApplication::class])
@ActiveProfiles("test")
@ContextConfiguration(initializers = [AbstractTestContainer.Initializer::class])
interface AbstractTestContainer {

    companion object {
        val mongoContainer = MongoDBContainer("mongo:7.0.14")
        val natsContainer = GenericContainer<Nothing>(DockerImageName.parse("nats:latest"))
            .apply { withExposedPorts(4222) }
    }

    class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            mongoContainer.start()
            natsContainer.start()
            TestPropertyValues.of(
                "spring.data.mongodb.uri=${mongoContainer.replicaSetUrl}",
                "nats.connection-uri=nats://${natsContainer.host}:${natsContainer.getMappedPort(4222)}"
            ).applyTo(configurableApplicationContext.environment)
        }
    }
}
