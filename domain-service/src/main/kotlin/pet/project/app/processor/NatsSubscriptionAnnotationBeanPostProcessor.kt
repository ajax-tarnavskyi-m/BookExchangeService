package pet.project.app.processor

import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.DynamicMessage
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
            connectToNats(bean)
        }
        return bean
    }

    private fun connectToNats(controllerInstance: Any) {
        val controllerAnnotation = controllerInstance.javaClass.getAnnotation(NatsController::class.java)

        controllerInstance.javaClass.methods
            .filter { it.isAnnotationPresent(NatsHandler::class.java) }
            .forEach { method -> subscribeToNats(controllerInstance, method, controllerAnnotation) }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun subscribeToNats(controllerObj: Any, handlerMethod: Method, controllerAnnotation: NatsController) {
        val subject = getHandlerSubject(controllerAnnotation, handlerMethod.getAnnotation(NatsHandler::class.java))
        val parser = getParser(handlerMethod)

        dispatcher.subscribe(subject, controllerAnnotation.queueGroup) { message ->
            try {
                val request = parser.parseFrom(message.data)
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
        return if (controllerAnnotation.subjectPrefix.isBlank()) {
            methodAnnotation.subject
        } else {
            "${controllerAnnotation.subjectPrefix}.${methodAnnotation.subject}"
        }
    }

    private fun buildErrorResponse(handlerMethod: Method, exception: Throwable): DynamicMessage {
        val responseDescriptor = getResponseTypeDescriptor(handlerMethod)
        val failureField = responseDescriptor.findFieldByName("failure")
        val exceptionMessage = exception.message ?: "Unknown error"

        return DynamicMessage.newBuilder(responseDescriptor)
            .setField(failureField, buildFailureMessage(failureField.messageType, exceptionMessage))
            .build()
    }

    private fun getResponseTypeDescriptor(handlerMethod: Method): Descriptor {
        val responseType = handlerMethod.genericReturnType as ParameterizedType
        val typeArgument = responseType.actualTypeArguments[0] as Class<*>
        return typeArgument.getMethod("getDescriptor").invoke(null) as Descriptor
    }

    private fun buildFailureMessage(failureDescriptor: Descriptor, exceptionMessage: String): DynamicMessage {
        val messageField = failureDescriptor.findFieldByName("message")
        return DynamicMessage.newBuilder(failureDescriptor)
            .setField(messageField, exceptionMessage)
            .build()
    }
}
