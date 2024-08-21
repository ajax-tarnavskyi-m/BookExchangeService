package pet.project.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BookExchangeServiceApplication

fun main(args: Array<String>) {
    @Suppress("SpreadOperator")
    runApplication<BookExchangeServiceApplication>(*args)
}
