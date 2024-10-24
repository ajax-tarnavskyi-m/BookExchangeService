package pet.project.app.annotation

import org.springframework.stereotype.Component

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
annotation class NatsController(
    val subjectPrefix: String = "",
    val queueGroup: String,
)
