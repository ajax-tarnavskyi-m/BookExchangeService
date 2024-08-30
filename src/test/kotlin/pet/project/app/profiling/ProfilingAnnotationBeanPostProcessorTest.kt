package pet.project.app.profiling

import com.mongodb.assertions.Assertions.assertTrue
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail
import pet.project.app.annotation.Profiling
import java.lang.reflect.Proxy
import kotlin.time.Duration

@ExtendWith(MockKExtension::class)
class ProfilingAnnotationBeanPostProcessorTest {

    @MockK
    private lateinit var profilingConsumer: ProfilingConsumer

    @InjectMockKs
    private lateinit var processor: ProfilingAnnotationBeanPostProcessor

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
        val expectedMethod = ProxyRequiredInterface::class.java.getDeclaredMethod("testMethod")

        // WHEN
        val profilingProxy = processor.postProcessAfterInitialization(bean, beanName)
        (profilingProxy as ProxyRequiredInterface).testMethod()

        // THEN
        verify(exactly = 1) {
            profilingConsumer.accept(match { profilingData ->
                profilingData.method == expectedMethod && profilingData.duration > Duration.ZERO
            })
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
