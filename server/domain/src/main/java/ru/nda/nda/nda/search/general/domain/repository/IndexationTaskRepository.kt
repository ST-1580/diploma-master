package nda.search.general.domain.repository

import nda.search.general.domain.model.ActivityStatus
import nda.search.general.domain.model.IndexationTask
import nda.search.general.domain.model.requests.IndexationTaskStartRq
import nda.search.general.domain.model.requests.IndexationTaskUpdateRq
import nda.search.general.domain.model.requests.IndexationTasksCreateRq
import nda.search.general.domain.model.requests.IndexationTasksStopRq

interface IndexationTaskRepository {

    fun createTasks(indexationTasksCreateRq: IndexationTasksCreateRq): List<IndexationTask>

    fun startTasks(indexationTaskStartRqs: List<IndexationTaskStartRq>)

    fun getTaskUidsByIndexationId(indexationId: Long): List<String>

    fun stopTasks(indexationTasksStopRq: IndexationTasksStopRq): List<IndexationTask>

    fun updateIndexationData(
        indexationId: Long,
        runnerName: String,
        indexationTaskUpdateRq: IndexationTaskUpdateRq
    )

    fun getTasksByIndexationId(
        indexationId: Long,
        taskStatus: ActivityStatus = ActivityStatus.ACTIVE
    ): List<IndexationTask>

    fun getTaskByIndexationIdAndRunnerName(
        indexationId: Long,
        runnerName: String,
        taskStatus: ActivityStatus = ActivityStatus.ACTIVE
    ): IndexationTask?
}
