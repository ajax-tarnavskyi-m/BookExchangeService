package pet.project.app.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pet.project.core.exception.handler.GlobalExceptionHandler

@Configuration
class ExceptionHandlerConfig {
    @Bean
    fun exceptionHandler(): GlobalExceptionHandler {
        return GlobalExceptionHandler()
    }
}
