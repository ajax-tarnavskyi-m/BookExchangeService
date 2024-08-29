package pet.project.app.util.profiling

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LoggingProfilingConsumer : ProfilingConsumer {
    override fun accept(message: String) {
        log.info(message)
    }


    companion object {
        private val log = LoggerFactory.getLogger(ProfilingAnnotationBeanPostProcessor::class.java)
    }

}
