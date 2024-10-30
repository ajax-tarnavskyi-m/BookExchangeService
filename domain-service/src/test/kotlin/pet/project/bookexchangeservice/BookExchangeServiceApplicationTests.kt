package pet.project.bookexchangeservice

import org.hibernate.validator.internal.util.Contracts.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import pet.project.app.controller.BookController
import pet.project.app.controller.nats.UserNatsController
import pet.project.app.repository.AbstractTestContainer
import pet.project.app.service.BookService
import pet.project.app.service.UserService

class BookExchangeServiceApplicationTests : AbstractTestContainer {

    @Autowired
    lateinit var bookController: BookController

    @Autowired
    lateinit var userNatsController: UserNatsController

    @Autowired
    lateinit var bookService: BookService

    @Autowired
    lateinit var userService: UserService

    @Test
    fun contextLoads() {
        assertNotNull(bookController)
        assertNotNull(userNatsController)
        assertNotNull(bookService)
        assertNotNull(userService)
    }
}
