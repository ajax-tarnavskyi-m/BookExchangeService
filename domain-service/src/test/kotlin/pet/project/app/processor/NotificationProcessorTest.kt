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
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import pet.project.app.dto.user.UserNotificationDetails
import pet.project.app.repository.BookRepository
import pet.project.app.repository.UserRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import reactor.kotlin.core.publisher.toMono
import java.time.Duration
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class NotificationProcessorTest {
    @MockK
    lateinit var userRepositoryMock: UserRepository

    @MockK
    lateinit var bookRepositoryMock: BookRepository
    private lateinit var availableBooksSink: Sinks.Many<String>
    private lateinit var listAppender: ListAppender<ILoggingEvent>

    @BeforeEach
    fun setUp() {
        val logger = LoggerFactory.getLogger(NotificationProcessor::class.java) as Logger
        listAppender = ListAppender<ILoggingEvent>().apply { start() }
        logger.addAppender(listAppender)

        availableBooksSink = Sinks.many().unicast().onBackpressureBuffer()
    }

    @Test
    fun `should log user notification when bookId is emitted to sink`() {
        // GIVEN
        val bookId = ObjectId.get().toHexString()
        val userDetails = UserNotificationDetails("testUser", "email@test.com", setOf(bookId))

        val shouldBeNotifiedUpdated = 1L.toMono()
        every { bookRepositoryMock.updateShouldBeNotified(bookId, false) } returns shouldBeNotifiedUpdated
        every { userRepositoryMock.findAllSubscribersOf(listOf(bookId)) } returns Flux.just(userDetails)

        val bufferMaxAmountOfEvents = 1
        val bufferFiveMinuteInterval = Duration.parse("PT5M")
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

    @Test
    fun `should not log user notification when book shouldBeNotified field is false`() {
        // GIVEN
        val bookId = ObjectId.get().toHexString()

        val shouldBeModifiedWasNotUpdated = 0L.toMono()
        every { bookRepositoryMock.updateShouldBeNotified(bookId, false) } returns shouldBeModifiedWasNotUpdated

        val bufferMaxAmountOfEvents = 1
        val bufferFiveMinuteInterval = Duration.parse("PT5M")
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
        }
        verify(exactly = 0) { userRepositoryMock.findAllSubscribersOf(any()) }
        val logs = listAppender.list
        assertTrue(logs.isEmpty(), "Should not log anything when book should be modified was false")
    }

    @AfterEach
    fun tearDown() {
        availableBooksSink.tryEmitComplete()
    }
}
