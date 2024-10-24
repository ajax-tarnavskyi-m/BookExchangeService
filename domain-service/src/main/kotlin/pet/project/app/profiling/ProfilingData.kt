package pet.project.app.profiling

import java.lang.reflect.Method
import kotlin.time.Duration

data class ProfilingData(
    val method: Method,
    val duration: Duration,
)
