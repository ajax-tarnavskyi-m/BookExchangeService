package pet.project.app.util

import mu.KotlinLogging
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.cglib.proxy.Enhancer
import org.springframework.cglib.proxy.MethodInterceptor
import org.springframework.cglib.proxy.MethodProxy
import org.springframework.context.annotation.Configuration
import pet.project.app.annotation.Profiling
import java.lang.reflect.Method
import kotlin.reflect.KClass

@Configuration
class ProfilingAnnotationBeanPostProcessor : BeanPostProcessor {

    private var map = hashMapOf<String, KClass<out Any>>()

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        val beanClass: KClass<out Any> = bean::class
        if (beanClass.annotations.contains(Profiling())) {
            logger.info { "PROFILING: Remembering $beanName during 'before init'" }
            map[beanName] = beanClass
        }
        return super.postProcessBeforeInitialization(bean, beanName)
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        val beanClass: KClass<out Any>? = map[beanName]
        if (beanClass != null) {
            val enhancer = Enhancer()
            enhancer.setSuperclass(beanClass.java)
            enhancer.setCallback(MethodInterceptor(::withProfiling))
            return enhancer.create()
        } else {
            return super.postProcessAfterInitialization(bean, beanName)
        }
    }

    private fun withProfiling(obj: Any, method: Method, args: Array<out Any>?, proxy: MethodProxy): Any? {
        val before = System.nanoTime()
        val invokeSuper: Any? = proxy.invokeSuper(obj, args ?: emptyArray())
        val after = System.nanoTime()
        logger.info { "${obj::class.simpleName}.${method.name} ran for ${after - before} nano" }
        return invokeSuper
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }

}
