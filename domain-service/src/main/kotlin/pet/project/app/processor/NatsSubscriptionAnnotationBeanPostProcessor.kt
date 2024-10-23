package pet.project.app.processor

import com.google.protobuf.GeneratedMessage
import com.google.protobuf.Parser
import io.nats.client.Connection
import io.nats.client.Dispatcher
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import pet.project.app.annotation.NatsController
import pet.project.app.annotation.NatsHandler
import reactor.core.publisher.Mono
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass

@Component
class NatsSubscriptionAnnotationBeanPostProcessor(
    private val connection: Connection,
    private val dispatcher: Dispatcher,
) : BeanPostProcessor {

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        val beanClass: KClass<out Any> = bean::class
        if (beanClass.annotations.any { it is NatsController }) {
            val controllerAnnotation = bean.javaClass.getAnnotation(NatsController::class.java)
            bean.javaClass.methods
                .filter { it.isAnnotationPresent(NatsHandler::class.java) }
                .forEach { method -> subscribeToNats(bean, method, controllerAnnotation) }
        }
        return bean
    }

    @Suppress("TooGenericExceptionCaught")
    private fun subscribeToNats(controllerObj: Any, handlerMethod: Method, controllerAnnotation: NatsController) {
        val subject = getHandlerSubject(controllerAnnotation, handlerMethod.getAnnotation(NatsHandler::class.java))

        dispatcher.subscribe(subject, controllerAnnotation.queueGroup) { message ->
            try {
                val request = getParser(handlerMethod).parseFrom(message.data)
                val response = handlerMethod.invoke(controllerObj, request) as Mono<GeneratedMessage>
                response.subscribe { connection.publish(message.replyTo, it.toByteArray()) }
            } catch (e: RuntimeException) {
                connection.publish(message.replyTo, buildErrorResponse(handlerMethod, e).toByteArray())
            }
        }
    }

    private fun getParser(method: Method): Parser<*> {
        val requestType = method.parameters.map { it.type }.first()
        return requestType.getMethod("parser").invoke(null) as Parser<*>
    }

    private fun getHandlerSubject(controllerAnnotation: NatsController, methodAnnotation: NatsHandler): String {
        return if (controllerAnnotation.subjectPrefix.isEmpty()) {
            methodAnnotation.subject
        } else {
            "${controllerAnnotation.subjectPrefix}.${methodAnnotation.subject}"
        }
    }

    private fun buildErrorResponse(handlerMethod: Method, exception: Throwable): GeneratedMessage {
        val responseType = handlerMethod.genericReturnType as ParameterizedType
        val typeArgument = responseType.actualTypeArguments[0] as Class<*>
        val builder = typeArgument.getMethod("newBuilder").invoke(null)

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
