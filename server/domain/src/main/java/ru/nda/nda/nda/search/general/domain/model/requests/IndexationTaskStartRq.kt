package nda.search.general.domain.model.requests

import nda.search.general.domain.model.ActivityStatus
import nda.search.general.domain.model.IndexationTask

data class IndexationTaskStartRq(
    val indexationId: Long,
    val lastIndexedEntityTs: Long,
    val runnerName: String,
    val entitiesWithIndexedTsCnt: Int,
    val taskStatus: ActivityStatus,
    val taskUid: String?,
) {
    constructor(indexationTask: IndexationTask, taskUid: String? = null) : this(
        indexationId = indexationTask.indexationId,
        lastIndexedEntityTs = indexationTask.lastIndexedEntityTs,
        runnerName = indexationTask.runnerName,
        entitiesWithIndexedTsCnt = indexationTask.entitiesWithIndexedTsCnt,
        taskStatus = indexationTask.taskStatus,
        taskUid = taskUid,
    )
}
