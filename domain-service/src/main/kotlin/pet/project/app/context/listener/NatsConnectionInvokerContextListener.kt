package pet.project.app.context.listener

import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.DynamicMessage
import com.google.protobuf.GeneratedMessage
import com.google.protobuf.Parser
import io.nats.client.Connection
import io.nats.client.Dispatcher
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import pet.project.app.annotation.NatsController
import pet.project.app.annotation.NatsHandler
import reactor.core.publisher.Mono
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

@Component
class NatsConnectionInvokerContextListener(
    private val connection: Connection,
    private val dispatcher: Dispatcher,
    private val factory: ConfigurableListableBeanFactory,
) : ApplicationListener<ApplicationReadyEvent> {

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        val applicationContext = event.applicationContext
        val beanDefinitionNames = applicationContext.beanDefinitionNames
        for (beanDefinitionName in beanDefinitionNames) {
            val originalClassName = factory.getBeanDefinition(beanDefinitionName).beanClassName
            if (isNatsController(originalClassName)) {
                val originalClass = Class.forName(originalClassName)
                val currentBean = applicationContext.getBean(beanDefinitionName)
                connectToNats(currentBean, originalClass)
            }
        }
    }

    private fun isNatsController(originalClassName: String?): Boolean {
        return originalClassName != null && Class.forName(originalClassName)
            .isAnnotationPresent(NatsController::class.java)
    }

    private fun connectToNats(currentBean: Any, originalClass: Class<*>) {
        val queueGroup = originalClass.getAnnotation(NatsController::class.java).queueGroup
        originalClass.methods
            .filter { originalMethod -> originalMethod.isAnnotationPresent(NatsHandler::class.java) }
            .forEach { originalMethod -> subscribeToNats(currentBean, originalMethod, queueGroup) }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun subscribeToNats(controllerBean: Any, originalMethod: Method, queueGroup: String) {
        val subject = originalMethod.getAnnotation(NatsHandler::class.java).subject
        val parser = getParser(originalMethod)
        val beanMethod = controllerBean.javaClass.getMethod(originalMethod.name, originalMethod.parameterTypes.first())
        dispatcher.subscribe(subject, queueGroup) { message ->
            try {
                val request = parser.parseFrom(message.data)
                val response = beanMethod.invoke(controllerBean, request) as Mono<GeneratedMessage>
                response.subscribe { connection.publish(message.replyTo, it.toByteArray()) }
            } catch (e: RuntimeException) {
                connection.publish(message.replyTo, buildErrorResponse(originalMethod, e).toByteArray())
            }
        }
    }

    private fun getParser(method: Method): Parser<*> {
        val requestType = method.parameters.map { it.type }.first()
        return requestType.getMethod("parser").invoke(null) as Parser<*>
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
