package pet.project.app.profiling

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LoggingProfilingConsumer : ProfilingConsumer {
    override fun accept(profilingData: ProfilingData) {
        log.info(
            "Method [{}.{}] ran for {} ns",
            profilingData.method.declaringClass.simpleName,
            profilingData.method.name,
            profilingData.duration.inWholeNanoseconds
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(ProfilingAnnotationBeanPostProcessor::class.java)
    }

}
