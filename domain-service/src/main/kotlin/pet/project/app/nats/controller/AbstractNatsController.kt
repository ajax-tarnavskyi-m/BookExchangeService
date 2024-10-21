package pet.project.app.nats.controller

import pet.project.app.annotation.NatsController
import pet.project.app.annotation.NatsHandler
import com.google.protobuf.GeneratedMessage
import com.google.protobuf.Parser
import io.nats.client.Connection
import io.nats.client.Dispatcher
import jakarta.annotation.PostConstruct
import reactor.core.publisher.Mono
import java.lang.reflect.ParameterizedType

abstract class AbstractNatsController(
    private val connection: Connection,
    private val dispatcher: Dispatcher,
) {
    @PostConstruct
    fun init() {
        initializeDispatchers()
    }

    @SuppressWarnings("TooGenericExceptionCaught")
    private fun initializeDispatchers() {
        val classAnnotation = this::class.java.getAnnotation(NatsController::class.java)
        val queueGroup = classAnnotation.queueGroup
        val methods = this::class.java.methods.filter { it.isAnnotationPresent(NatsHandler::class.java) }

        for (method in methods) {
            val methodAnnotation = method.getAnnotation(NatsHandler::class.java)
            val requestType = method.parameters.map { it.type }.first()
            val responseType = method.genericReturnType as ParameterizedType
            val typeArgument = responseType.actualTypeArguments[0] as Class<*>
            val parser = requestType.getMethod("parser").invoke(null) as Parser<*>
            val subject = if (classAnnotation.subjectPrefix.isEmpty()) {
                methodAnnotation.subject
            } else {
                "${classAnnotation.subjectPrefix}.${methodAnnotation.subject}"
            }

            dispatcher.subscribe(subject, queueGroup) { message ->
                try {
                    val request = parser.parseFrom(message.data)
                    val response = method.invoke(this, request) as Mono<GeneratedMessage>
                    response
                        .subscribe { connection.publish(message.replyTo, it.toByteArray()) }
                } catch (e: RuntimeException) {
                    connection.publish(message.replyTo, buildErrorResponse(typeArgument, e).toByteArray())
                }
            }
        }
    }

    private fun buildErrorResponse(returnType: Class<*>, exception: Throwable): GeneratedMessage {
        val builder = returnType.getMethod("newBuilder").invoke(null)

        val failureBuilderMethod = builder.javaClass.getMethod("getFailureBuilder")
        val failureBuilder = failureBuilderMethod.invoke(builder)

        val setMessageMethod = failureBuilder.javaClass.getMethod("setMessage", String::class.java)
        setMessageMethod.invoke(failureBuilder, exception.message ?: "Unknown error")

        val failureMessage = failureBuilder.javaClass.getMethod("build").invoke(failureBuilder)

        val setFailureMethod = builder.javaClass.getMethod("setFailure", failureMessage.javaClass)
        setFailureMethod.invoke(builder, failureMessage)

        return builder.javaClass.getMethod("build").invoke(builder) as GeneratedMessage
    }
}
