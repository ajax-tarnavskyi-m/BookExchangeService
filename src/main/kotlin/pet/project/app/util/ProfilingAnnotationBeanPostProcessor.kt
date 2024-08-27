package pet.project.app.util

import org.slf4j.LoggerFactory
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

    private val beanClassesForProfilingMap = hashMapOf<String, KClass<out Any>>()

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        val beanClass: KClass<out Any> = bean::class
        if (annotationExistsRecursively(beanClass, Profiling())) {
            log.info("PROFILING: Remembering {} during 'before init'", beanName)
            beanClassesForProfilingMap[beanName] = beanClass
        }
        return bean
    }

    private fun annotationExistsRecursively(beanClass: KClass<out Any>, annotation: Annotation): Boolean {
        return beanClass.annotations.contains(annotation)
                || beanClass.superclasses.any { annotationExistsRecursively(it, annotation) }
    }

    @Suppress("ReturnCount")
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        val beanClass = beanClassesForProfilingMap[beanName] ?: return bean

        val enhancer = Enhancer()
        enhancer.setSuperclass(beanClass.java)
        enhancer.setCallback(MethodInterceptor(::invokeWithProfiling))

        val constructorParams: Array<Class<*>> = getConstructorParams(beanClass) ?: return enhancer.create()
        constructorParams.forEach { log.info("constructor param: {}", it) }

        val propertyValues: Array<Any?> = getPropertyValues(beanClass, bean)
        propertyValues.forEach { log.info("property value: {}, type: {}", it, it?.javaClass?.name) }

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

    private fun invokeWithProfiling(
        obj: Any,
        method: Method,
        args: Array<out Any>?,
        proxy: MethodProxy,
    ): Any? {
        val before = System.nanoTime()
        return try {
            proxy.invokeSuper(obj, args ?: emptyArray())
        } finally {
            val after = System.nanoTime()
            log.info("{}.{} ran for {} nano", obj::class.simpleName, method.name, after - before)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(ProfilingAnnotationBeanPostProcessor::class.java)
    }

}
