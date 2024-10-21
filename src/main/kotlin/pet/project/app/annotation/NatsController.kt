package pet.project.app.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class NatsController(
    val subjectPrefix: String = "",
    val queueGroup: String,
)
