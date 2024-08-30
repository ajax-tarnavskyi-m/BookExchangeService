package pet.project.app.profiling

interface ProfilingConsumer {
    fun accept(profilingData: ProfilingData)
}
