package nda.search.general.application.indexation.tp.runners

import org.slf4j.LoggerFactory
import ru.nda.qe.tp.Context
import ru.nda.qe.tp.Task
import ru.nda.qe.tp.TaskBuilder
import ru.nda.qe.tp.TpRunner

abstract class AbstractRunner : TpRunner<Any?> {

    protected abstract val runnerName: String
    protected abstract val isIndexingEnabled: Boolean
    protected abstract val poolName: String

    fun newTask(): Task = TaskBuilder.builder()
        .setRunner(runnerName)
        .setPool(poolName)
        .build()

    override fun call(): Any? {
        val context = Context.get()
        try {
            doCall(context)
        } catch (e: Exception) {
            LOG.error("Task failed on pool $poolName, on runner $runnerName", e)
        }

        return null
    }

    abstract fun doCall(context: Context)

    override fun getName(): String = runnerName

    override fun isEnabled(): Boolean = isIndexingEnabled

    companion object {
        private val LOG = LoggerFactory.getLogger(AbstractRunner::class.java)
    }
}
