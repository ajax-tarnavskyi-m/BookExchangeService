package pet.project.app.profiling

import com.mongodb.assertions.Assertions.assertNotNull
import com.mongodb.assertions.Assertions.assertTrue
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.aopalliance.intercept.MethodInvocation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.aop.support.AopUtils
import pet.project.app.annotation.Profiling
import java.lang.reflect.Method
import kotlin.time.Duration

@ExtendWith(MockKExtension::class)
class ProfilingAnnotationBeanPostProcessorTest {

    @MockK
    private lateinit var profilingConsumer: ProfilingConsumer

    @InjectMockKs
    private lateinit var processor: ProfilingAnnotationBeanPostProcessor

    @Test
    fun `postProcessAfterInitialization should return CGLIB proxy when bean is annotated with Profiling`() {
        // GIVEN
        val bean = ProfilingAnnotatedClass()
        val beanName = "testBean"
        every { profilingConsumer.accept(any()) } just runs
        processor.postProcessBeforeInitialization(bean, beanName)

        // WHEN
        val result = processor.postProcessAfterInitialization(bean, beanName)

        // THEN
        assertNotNull(result)
        assertTrue(AopUtils.isCglibProxy(result))
    }

    @Profiling
    open class ProfilingAnnotatedClass

    @Test
    fun `postProcessAfterInitialization should return original bean when not annotated with Profiling`() {
        // GIVEN
        val bean = NonProfilingAnnotatedClass()
        val beanName = "testBean"

        // WHEN
        val result = processor.postProcessAfterInitialization(bean, beanName)

        // THEN
        assertEquals(bean, result)
    }

    open class NonProfilingAnnotatedClass

    @Nested
    inner class MethodInvocationProfilingTests {

        @MockK
        private lateinit var profilingConsumer: ProfilingConsumer

        @InjectMockKs
        private lateinit var interceptor: ProfilingAnnotationBeanPostProcessor.LoggerProfilingMethodInterceptor

        inner class CustomMethodInvocation(
            private val target: Any,
            private val method: Method,
            private val arguments: Array<Any>,
        ) : MethodInvocation {
            override fun getMethod(): Method = method
            override fun getArguments(): Array<Any> = arguments
            override fun getThis(): Any = target
            override fun getStaticPart(): Method = method
            override fun proceed(): Any? = method.invoke(target, *arguments)
        }

        @Test
        fun `invoke should profile method execution and return result`() {
            //GIVEN
            val method = InterceptedExampleClass::class.java.getDeclaredMethod("profiledMethod")
            val methodInvocationSpy = CustomMethodInvocation(InterceptedExampleClass(), method, emptyArray())
            val expectedInvocationResult = TEST_INVOCATION_RESULT
            every { profilingConsumer.accept(any()) } just runs

            // WHEN
            val actualInvocationResult = interceptor.invoke(methodInvocationSpy)

            // THEN
            verify(exactly = 1) {
                profilingConsumer.accept(match { profilingData ->
                    profilingData.method == method && profilingData.duration > Duration.ZERO
                })
            }
            assertEquals(expectedInvocationResult, actualInvocationResult)
        }

        inner class InterceptedExampleClass {
            fun profiledMethod() = TEST_INVOCATION_RESULT
        }

    }

    companion object {
        private const val TEST_INVOCATION_RESULT = "TEST_INVOCATION_RESULT"
    }
}
