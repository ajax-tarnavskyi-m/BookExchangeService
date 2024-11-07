package pet.project.app.processor

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import pet.project.app.dto.user.UserNotificationDetails
import pet.project.app.kafka.BookAmountIncreasedProducer
import pet.project.app.repository.BookRepository
import pet.project.app.repository.UserRepository
import pet.project.core.RandomTestFields.Book.randomBookIdString
import pet.project.core.RandomTestFields.User.randomEmail
import pet.project.core.RandomTestFields.User.randomLogin
import reactor.core.publisher.Flux
import reactor.kafka.receiver.KafkaReceiver
import reactor.kotlin.core.publisher.toMono
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.test.Test

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockKExtension::class)
class NotificationProcessorTest {

    @MockK
    private lateinit var bookRepository: BookRepository

    @MockK
    private lateinit var userRepository: UserRepository

    @Autowired
    lateinit var bookAmountIncreasedProducer: BookAmountIncreasedProducer

    @Autowired
    lateinit var kafkaReceiver: KafkaReceiver<String, ByteArray>

    @Test
    fun `should call NotifyUser() when bookId is sent to Kafka`() {
        // GIVEN
        val bookId = randomBookIdString()
        every { bookRepository.updateShouldBeNotified(bookId, false) } returns 1L.toMono()
        every { bookRepository.updateShouldBeNotified(neq(bookId), false) } returns 0L.toMono()
        val userDetails = UserNotificationDetails(randomLogin(), randomEmail(), setOf(bookId))
        every { userRepository.findAllSubscribersOf(listOf(bookId)) } returns Flux.just(userDetails)

        val logger: Logger = LoggerFactory.getLogger(NotificationProcessor::class.java) as Logger
        val listAppender = ListAppender<ILoggingEvent>().apply { start() }
        logger.addAppender(listAppender)
        val subscription = NotificationProcessor(
            bookRepository,
            userRepository,
            kafkaReceiver,
            notificationInterval = Duration.ofMinutes(5),
            notificationMaxAmount = 1,
        ).subscribeToReceiver()

        // WHEN
        bookAmountIncreasedProducer.sendMessages(listOf(bookId))

        // THEN
        await()
            .atMost(15, TimeUnit.SECONDS)
            .untilAsserted {
                verify { bookRepository.updateShouldBeNotified(bookId, false) }
                verify { userRepository.findAllSubscribersOf(listOf(bookId)) }
            }
        val logs = listAppender.list.map { it.formattedMessage }
        val expected = "Hi ${userDetails.login}! There is new books for you: ${userDetails.bookTitles}"
        assertTrue(logs.contains(expected), "Should contain log message with generated data")
        subscription.dispose()
    }
}
