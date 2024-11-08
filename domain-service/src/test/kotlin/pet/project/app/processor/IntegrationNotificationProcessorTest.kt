package pet.project.app.processor

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Assertions.assertTrue
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.service.BookService
import pet.project.app.service.UserService
import pet.project.core.RandomTestFields.Book.randomDescription
import pet.project.core.RandomTestFields.Book.randomPrice
import pet.project.core.RandomTestFields.Book.randomTitle
import pet.project.core.RandomTestFields.Book.randomYearOfPublishing
import pet.project.core.RandomTestFields.User.randomEmail
import pet.project.core.RandomTestFields.User.randomLogin
import pet.project.internal.input.reqreply.user.CreateUserRequest
import java.util.concurrent.TimeUnit
import kotlin.test.Test

@SpringBootTest
class IntegrationNotificationProcessorTest {

    @Autowired
    lateinit var bookService: BookService

    @Autowired
    lateinit var userService: UserService

    @Test
    fun `should notify user(log) when subscribed books available`() {
        // GIVEN
        val newBooks = (1..10).map { bookService.create(createZeroAmountBookRequest()) }.map { it.block()!! }
        val userRequest = CreateUserRequest.newBuilder()
            .setLogin(randomLogin())
            .setEmail(randomEmail())
            .addAllBookWishList(newBooks.map { it.id })
            .build()
        userService.create(userRequest).block()!!
        val expectedTitles = newBooks.map { it.title }

        val logger: Logger = LoggerFactory.getLogger(NotificationProcessor::class.java) as Logger
        val listAppender = ListAppender<ILoggingEvent>().apply { start() }
        logger.addAppender(listAppender)

        // WHEN
        bookService.exchangeBooks(newBooks.map { UpdateAmountRequest(it.id, 1) }).block()!!

        // THEN
        await()
            .atMost(15, TimeUnit.SECONDS)
            .untilAsserted {
                assertTrue(
                    listAppender.list.map { it.formattedMessage }
                        .any { it.contains(userRequest.login) && expectedTitles.all { title -> it.contains(title) } },
                    "Should notify(log) specific user about 10 books availability"
                )
            }
    }

    private fun createZeroAmountBookRequest() = CreateBookRequest(
        randomTitle(),
        randomDescription(),
        randomYearOfPublishing(),
        randomPrice(),
        0
    )
}
