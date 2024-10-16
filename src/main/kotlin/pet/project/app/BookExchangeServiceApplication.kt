package pet.project.app

import io.mongock.runner.springboot.EnableMongock
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@EnableMongock
@SpringBootApplication
class BookExchangeServiceApplication

fun main(args: Array<String>) {
    @Suppress("SpreadOperator")
    runApplication<BookExchangeServiceApplication>(*args)
}
