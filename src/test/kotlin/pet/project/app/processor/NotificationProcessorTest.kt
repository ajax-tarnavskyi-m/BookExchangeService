package pet.project.app.processor

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import pet.project.app.dto.user.UserNotificationDetails
import pet.project.app.repository.BookRepository
import pet.project.app.repository.UserRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks

@ExtendWith(MockKExtension::class)
class NotificationProcessorTest {
    @MockK
    lateinit var userRepositoryMock: UserRepository

    @MockK
    lateinit var bookRepositoryMock: BookRepository
    private val availableBooksSink = Sinks.many().unicast().onBackpressureBuffer<String>()

    @Test
    fun `should log user login and book ID when sink buffer is full`() {
        // GIVEN
        val logger = LoggerFactory.getLogger(NotificationProcessor::class.java) as Logger
        val listAppender = ListAppender<ILoggingEvent>().apply { start() }
        logger.addAppender(listAppender)

        val bookId = "66bf6bf8039339103054e21a"
        val userDetails = UserNotificationDetails("testUser", "email@test.com", setOf(bookId))

        every { bookRepositoryMock.updateShouldBeNotified(bookId, false) } returns Mono.just(1L)
        every { userRepositoryMock.findAllSubscribersOf(listOf(bookId)) } returns Flux.just(userDetails)

        val bufferMaxAmountOfEvents = 1
        val bufferFiveMinuteInterval = "PT5M"
        val notificationProcessor = NotificationProcessor(
            bookRepositoryMock,
            userRepositoryMock,
            availableBooksSink,
            bufferFiveMinuteInterval,
            bufferMaxAmountOfEvents
        )

        // WHEN
        notificationProcessor.subscribeToSink()
        availableBooksSink.tryEmitNext(bookId)

        // THEN
        await().untilAsserted {
            verify { bookRepositoryMock.updateShouldBeNotified(bookId, false) }
            verify { userRepositoryMock.findAllSubscribersOf(listOf(bookId)) }
        }
        val logs = listAppender.list
        val expectedMessage = "Hi ${userDetails.login}! There is new books for you: ${userDetails.bookTitles}"
        assertEquals(expectedMessage, logs.first().formattedMessage)
        assertEquals(Level.INFO, logs.first().level)
    }

    @AfterEach
    fun tearDown() {
        availableBooksSink.tryEmitComplete()
    }
}
