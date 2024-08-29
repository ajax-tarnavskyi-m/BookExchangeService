package pet.project.app.util.profiling

import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import pet.project.app.annotation.Profiling
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

@Component
class ProfilingAnnotationBeanPostProcessor(private val profilingConsumer: ProfilingConsumer) : BeanPostProcessor {

    private val beanClassesForProfilingMap = hashMapOf<String, Class<*>>()

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        val beanClass: Class<*> = bean::class.java
        if (beanClass.annotations.contains(Profiling())) {
            profilingConsumer.accept("PROFILING: Remembering $beanName during 'before init'")
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
    class ProfilingInvocationHandler(
        private val target: Any,
        private val profilingConsumer: ProfilingConsumer,
    ) : InvocationHandler {

        override fun invoke(proxy: Any?, method: Method, args: Array<Any>?): Any? {
            val before = System.nanoTime()
            return try {
                method.invoke(target, *(args ?: emptyArray()))
            } finally {
                val after = System.nanoTime()
                val className = target::class.simpleName
                profilingConsumer.accept("$className.${method.name} method ran for ${after - before} ns")
            }
        }
    }

}
