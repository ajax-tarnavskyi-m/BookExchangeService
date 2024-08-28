package pet.project.app.util

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import pet.project.app.annotation.Profiling
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

@Component
class ProfilingAnnotationBeanPostProcessor : BeanPostProcessor {

    private val beanClassesForProfilingMap = hashMapOf<String, Class<*>>()

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        val beanClass: Class<*> = bean::class.java
        if (beanClass.annotations.contains(Profiling())) {
            log.info("PROFILING: Remembering {} during 'before init'", beanName)
            beanClassesForProfilingMap[beanName] = beanClass
        }
        return bean
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        val beanClass = beanClassesForProfilingMap[beanName] ?: return bean

        return Proxy.newProxyInstance(beanClass.classLoader, beanClass.interfaces, ProfilingInvocationHandler(bean))
    }

    @Suppress("SpreadOperator")
    class ProfilingInvocationHandler(private val target: Any) : InvocationHandler {
        override fun invoke(proxy: Any?, method: Method, args: Array<Any>?): Any? {
            val before = System.nanoTime()
            return try {
                method.invoke(target, *(args ?: emptyArray()))
            } finally {
                val after = System.nanoTime()
                log.info("{}.{} method ran for {} ns", target::class.simpleName, method.name, after - before)
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(ProfilingAnnotationBeanPostProcessor::class.java)
    }

}
