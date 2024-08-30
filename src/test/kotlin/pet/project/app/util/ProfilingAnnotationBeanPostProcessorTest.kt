package pet.project.app.util

import com.mongodb.assertions.Assertions.assertTrue
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import pet.project.app.annotation.Profiling
import pet.project.app.util.profiling.ProfilingAnnotationBeanPostProcessor
import pet.project.app.util.profiling.ProfilingConsumer
import java.lang.reflect.Proxy

class ProfilingAnnotationBeanPostProcessorTest {

    @MockK
    private lateinit var profilingConsumer: ProfilingConsumer

    @InjectMockKs
    private lateinit var processor: ProfilingAnnotationBeanPostProcessor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `postProcessBeforeInitialization should remember beans with Profiling annotation`() {
        // GIVEN
        val bean = ProfilingAnnotatedClass()
        val beanName = "testBean"

        every { profilingConsumer.accept(any()) } just runs

        // WHEN
        val result = processor.postProcessBeforeInitialization(bean, beanName)

        // THEN
        assertEquals(bean, result)
        verify { profilingConsumer.accept("PROFILING: Remembering testBean during 'before init'") }
    }

    @Test
    fun `postProcessBeforeInitialization should ignore beans without Profiling annotation`() {
        // GIVEN
        val bean = NonProfilingAnnotatedClass()
        val beanName = "testBean"

        // WHEN
        val result = processor.postProcessBeforeInitialization(bean, beanName)

        // THEN
        assertEquals(bean, result)
        verify(exactly = 0) { profilingConsumer.accept(any()) }
    }

    @Test
    fun `postProcessAfterInitialization should return proxy for beans with Profiling annotation`() {
        // GIVEN
        val bean = ProfilingAnnotatedClass()
        val beanName = "testBean"
        every { profilingConsumer.accept(any()) } just runs
        processor.postProcessBeforeInitialization(bean, beanName)

        // WHEN
        val result = processor.postProcessAfterInitialization(bean, beanName)

        // THEN
        if (result == null) fail("Should not return null value from afterInit method")
        assertTrue(Proxy.isProxyClass(result::class.java))
    }

    @Test
    fun `postProcessAfterInitialization should return original bean if no Profiling annotation`() {
        // GIVEN
        val bean = NonProfilingAnnotatedClass()
        val beanName = "testBean"

        // WHEN
        val result = processor.postProcessAfterInitialization(bean, beanName)

        // THEN
        assertEquals(bean, result)
    }

    @Test
    fun `postProcessAfterInitialization should measure method execution time`() {
        // GIVEN
        val bean = ProfilingAnnotatedClass()
        val beanName = "testBean"
        every { profilingConsumer.accept(any()) } just runs
        processor.postProcessBeforeInitialization(bean, beanName)

        // WHEN
        val profilingProxy = processor.postProcessAfterInitialization(bean, beanName)
        (profilingProxy as ProxyRequiredInterface).testMethod()

        // THEN
        verify(exactly = 1) { profilingConsumer.accept("PROFILING: Remembering testBean during 'before init'") }
        verify(exactly = 1) {
            profilingConsumer.accept(
                match { message ->
                    message.startsWith("ProfilingAnnotatedClass.testMethod method ran for ") &&
                            message.endsWith(" ns")
                }
            )
        }
    }

    @Test
    fun `postProcessAfterInitialization should not measure method execution time when no profiling annotation`() {
        // GIVEN
        val bean = NonProfilingAnnotatedClass()
        val beanName = "testBean"
        every { profilingConsumer.accept(any()) } just runs
        processor.postProcessBeforeInitialization(bean, beanName)

        // WHEN
        val profilingProxy = processor.postProcessAfterInitialization(bean, beanName)
        (profilingProxy as ProxyRequiredInterface).testMethod()

        // THEN
        verify(exactly = 0) { profilingConsumer.accept(any()) }
    }

    interface ProxyRequiredInterface {
        fun testMethod()
    }

    @Profiling
    class ProfilingAnnotatedClass : ProxyRequiredInterface {
        override fun testMethod() = Unit
    }

    class NonProfilingAnnotatedClass : ProxyRequiredInterface {
        override fun testMethod() = Unit
    }
}
