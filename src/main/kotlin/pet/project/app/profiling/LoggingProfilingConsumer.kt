package pet.project.app.profiling

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LoggingProfilingConsumer : ProfilingConsumer {
    override fun accept(profilingData: ProfilingData) {
        val className = profilingData.method.declaringClass.simpleName
        val methodName = profilingData.method.name
        val nanoseconds = profilingData.duration.inWholeNanoseconds
        log.info("$className.$methodName method ran for $nanoseconds ns")
    }

    companion object {
        private val log = LoggerFactory.getLogger(ProfilingAnnotationBeanPostProcessor::class.java)
    }

}
