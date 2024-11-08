package pet.project.app

import org.hibernate.validator.internal.util.Contracts.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import pet.project.app.controller.BookController
import pet.project.app.controller.nats.UserNatsController
import pet.project.app.repository.BookRepository
import pet.project.app.service.BookService
import pet.project.app.service.UserService

@SpringBootTest
@ActiveProfiles("test")
class BookExchangeServiceApplicationTests {

    @Autowired
    lateinit var bookController: BookController

    @Autowired
    lateinit var userNatsController: UserNatsController

    @Autowired
    lateinit var bookService: BookService

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var bookRepository: BookRepository

    @Test
    fun contextLoads() {
        assertNotNull(bookRepository)
        assertNotNull(bookController)
        assertNotNull(userNatsController)
        assertNotNull(bookService)
        assertNotNull(userService)
    }
}
