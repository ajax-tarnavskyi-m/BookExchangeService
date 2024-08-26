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
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.jvmErasure

@Configuration
class ProfilingAnnotationBeanPostProcessor : BeanPostProcessor {

    private var map = hashMapOf<String, KClass<out Any>>()

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        val beanClass: KClass<out Any> = bean::class
        if (findAnnotationRecursively(beanClass, Profiling())) {
            logger.info { "PROFILING: Remembering $beanName during 'before init'" }
            map[beanName] = beanClass
        }
        return bean
    }

    private fun findAnnotationRecursively(beanClass: KClass<out Any>, annotation: Annotation): Boolean {
        if (beanClass.annotations.contains(annotation)) {
            return true
        }
        return beanClass.superclasses.any { findAnnotationRecursively(it, annotation) }
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        val beanClass = map[beanName] ?: return bean

        val enhancer = Enhancer()
        enhancer.setSuperclass(beanClass.java)
        enhancer.setCallback(MethodInterceptor(::withProfiling))

        val constructorParams: Array<Class<*>> = getConstructorParams(beanClass) ?: return enhancer.create()
        constructorParams.forEach { logger.info { "constructor param: $it" } }

        val propertyValues: Array<Any?> = getPropertyValues(beanClass, bean)
        propertyValues.forEach { logger.info { "property value: $it, type: ${it?.javaClass?.name}" } }

        return enhancer.create(constructorParams, propertyValues)
    }

    private fun getPropertyValues(beanClass: KClass<out Any>, bean: Any): Array<Any?> {
        val kotlinProperties = beanClass.memberProperties.map { it.javaField }
        val allJavaFields = beanClass.java.declaredFields
        allJavaFields.forEach { it.isAccessible = true }
        return allJavaFields
            .filter { kotlinProperties.contains(it) }
            .map { it.get(bean) }
            .toTypedArray()
    }

    private fun getConstructorParams(beanClass: KClass<out Any>): Array<Class<*>>? {
        return beanClass.primaryConstructor?.parameters?.map { it.type.jvmErasure.java }?.toTypedArray()
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
