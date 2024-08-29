package pet.project.app.util

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import pet.project.app.annotation.Profiling
import pet.project.app.util.profiling.ProfilingAnnotationBeanPostProcessor
import pet.project.app.util.profiling.ProfilingConsumer

class ProfilingAnnotationBeanPostProcessorTest {

    @MockK
    private lateinit var profilingConsumer : ProfilingConsumer

    @InjectMockKs
    private lateinit var processor: ProfilingAnnotationBeanPostProcessor

    @Profiling
    class ProfilingAnnotatedClass {
        fun dummyMethod() {

        }
    }

}
