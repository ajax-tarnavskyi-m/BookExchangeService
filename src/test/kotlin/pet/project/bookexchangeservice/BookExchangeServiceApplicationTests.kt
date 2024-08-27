package pet.project.bookexchangeservice

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pet.project.app.BookExchangeServiceApplication
import pet.project.app.controller.BookController
import pet.project.app.controller.UserController
import pet.project.app.service.BookService
import pet.project.app.service.UserService
import kotlin.test.assertNotNull

@SpringBootTest(classes = [BookExchangeServiceApplication::class])
class BookExchangeServiceApplicationTests {

    @Autowired
    lateinit var bookController: BookController
    @Autowired
    lateinit var userController: UserController
    @Autowired
    lateinit var bookService : BookService
    @Autowired
    lateinit var userService: UserService

    @Test
    fun contextLoads() {
        assertNotNull(bookController)
        assertNotNull(userController)
        assertNotNull(bookService)
        assertNotNull(userService)
    }

}
