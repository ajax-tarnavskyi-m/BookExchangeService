package pet.project.app.profiling

import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import pet.project.app.annotation.Profiling
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.time.Duration.Companion.nanoseconds

@Component
class ProfilingAnnotationBeanPostProcessor(private val profilingConsumer: ProfilingConsumer) : BeanPostProcessor {

    private val beanClassesForProfilingMap = hashMapOf<String, Class<*>>()

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        val beanClass: Class<*> = bean::class.java
        if (beanClass.annotations.contains(Profiling())) {
            beanClassesForProfilingMap[beanName] = beanClass
        }
        return bean
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        val beanClass = beanClassesForProfilingMap[beanName] ?: return bean

        return Proxy.newProxyInstance(
            beanClass.classLoader,
            beanClass.interfaces,
            ProfilingInvocationHandler(bean, profilingConsumer)
        )
    }

    @Suppress("SpreadOperator")
    private class ProfilingInvocationHandler(
        private val target: Any,
        private val profilingConsumer: ProfilingConsumer,
    ) : InvocationHandler {

        override fun invoke(proxy: Any?, method: Method, args: Array<Any>?): Any? {
            val before = System.nanoTime().nanoseconds
            return try {
                method.invoke(target, *(args ?: emptyArray()))
            } finally {
                val after = System.nanoTime().nanoseconds
                profilingConsumer.accept(ProfilingData(method, after - before))
            }
        }
    }

}
