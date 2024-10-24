package pet.project.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GatewayApplication

@SuppressWarnings("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<GatewayApplication>(*args)
}
