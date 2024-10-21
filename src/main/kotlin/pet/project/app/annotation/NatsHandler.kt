package pet.project.app.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class NatsHandler(
    val subject: String,
)
