package nda.search.general.domain.repository.impl

import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import nda.search.general.domain.db.Tables.INDEXATION_TASK
import nda.search.general.domain.db.tables.records.IndexationTaskRecord
import nda.search.general.domain.model.ActivityStatus
import nda.search.general.domain.model.IndexationTask
import nda.search.general.domain.model.requests.IndexationTaskStartRq
import nda.search.general.domain.model.requests.IndexationTaskUpdateRq
import nda.search.general.domain.model.requests.IndexationTasksCreateRq
import nda.search.general.domain.model.requests.IndexationTasksStopRq
import nda.search.general.domain.repository.IndexationTaskRepository

@Repository
class IndexationTaskRepositoryImpl @Autowired constructor(
    private val dslContext: DSLContext,
) : IndexationTaskRepository {
    override fun createTasks(indexationTasksCreateRq: IndexationTasksCreateRq): List<IndexationTask> {
        val records = indexationTasksCreateRq.runners.map { toRecord(indexationTasksCreateRq, it) }

        return dslContext.transactionResult { ctx ->
            ctx.dsl()
                .batchInsert(records)
                .execute()

            ctx.dsl()
                .selectFrom(INDEXATION_TASK)
                .where(
                    INDEXATION_TASK.INDEXATION_ID.eq(indexationTasksCreateRq.indexationId)
                        .and(INDEXATION_TASK.ACTIVE_STATUS.eq(ActivityStatus.INACTIVE.name))
                )
                .fetch(::toModel)
        }
    }

    override fun startTasks(indexationTaskStartRqs: List<IndexationTaskStartRq>) {
        val records = indexationTaskStartRqs
            .filter { it.taskStatus == ActivityStatus.INACTIVE }
            .map { it.copy(taskStatus = ActivityStatus.ACTIVE) }
            .map(::toRecord)

        dslContext.batchUpdate(records).execute()

        LOG.debug("Ready to start ${records.size} task(s)")
    }

    override fun getTaskUidsByIndexationId(indexationId: Long): List<String> {
        return dslContext
            .select(INDEXATION_TASK.TASK_UID)
            .from(INDEXATION_TASK)
            .where(INDEXATION_TASK.INDEXATION_ID.eq(indexationId))
            .fetch()
            .getValues(INDEXATION_TASK.TASK_UID)
    }

    override fun stopTasks(indexationTasksStopRq: IndexationTasksStopRq): List<IndexationTask> {
        return dslContext
            .update(INDEXATION_TASK)
            .set(INDEXATION_TASK.ACTIVE_STATUS, ActivityStatus.INACTIVE.name)
            .where(INDEXATION_TASK.INDEXATION_ID.eq(indexationTasksStopRq.indexationId))
            .returning()
            .map(::toModel)
    }

    override fun updateIndexationData(
        indexationId: Long,
        runnerName: String,
        indexationTaskUpdateRq: IndexationTaskUpdateRq
    ) {
        dslContext
            .update(INDEXATION_TASK)
            .set(INDEXATION_TASK.LAST_INDEXED_ENTITY_TS, indexationTaskUpdateRq.lastIndexedEntityTs)
            .set(INDEXATION_TASK.ENTITIES_WITH_INDEXED_TS_CNT, indexationTaskUpdateRq.entitiesWithIndexedTsCnt)
            .where(
                INDEXATION_TASK.INDEXATION_ID.eq(indexationId)
                    .and(INDEXATION_TASK.RUNNER_NAME.eq(runnerName))
            )
            .execute()
    }

    override fun getTasksByIndexationId(indexationId: Long, taskStatus: ActivityStatus): List<IndexationTask> {
        return dslContext
            .selectFrom(INDEXATION_TASK)
            .where(
                INDEXATION_TASK.INDEXATION_ID.eq(indexationId)
                    .and(INDEXATION_TASK.ACTIVE_STATUS.eq(taskStatus.name))
            )
            .fetch(::toModel)
    }

    override fun getTaskByIndexationIdAndRunnerName(
        indexationId: Long,
        runnerName: String,
        taskStatus: ActivityStatus
    ): IndexationTask? {
        return dslContext
            .selectFrom(INDEXATION_TASK)
            .where(
                INDEXATION_TASK.INDEXATION_ID.eq(indexationId)
                    .and(INDEXATION_TASK.RUNNER_NAME.eq(runnerName))
                    .and(INDEXATION_TASK.ACTIVE_STATUS.eq(taskStatus.name))
            )
            .fetchOne(::toModel)
    }

    companion object {
        val LOG = LoggerFactory.getLogger(IndexationTaskRepositoryImpl::class.java)

        fun toModel(record: IndexationTaskRecord): IndexationTask {
            return IndexationTask(
                indexationId = record.indexationId,
                lastIndexedEntityTs = record.lastIndexedEntityTs,
                runnerName = record.runnerName,
                entitiesWithIndexedTsCnt = record.entitiesWithIndexedTsCnt,
                taskStatus = ActivityStatus.valueOf(record.activeStatus)
            )
        }

        fun toRecord(createRq: IndexationTasksCreateRq, runnerName: String): IndexationTaskRecord {
            return IndexationTaskRecord(
                createRq.indexationId,
                createRq.startedTs,
                runnerName,
                0,
                ActivityStatus.INACTIVE.name,
                null,
            )
        }

        fun toRecord(indexationTaskWithTpUid: IndexationTaskStartRq): IndexationTaskRecord {
            return IndexationTaskRecord(
                indexationTaskWithTpUid.indexationId,
                indexationTaskWithTpUid.lastIndexedEntityTs,
                indexationTaskWithTpUid.runnerName,
                indexationTaskWithTpUid.entitiesWithIndexedTsCnt,
                indexationTaskWithTpUid.taskStatus.name,
                indexationTaskWithTpUid.taskUid,
            )
        }
    }
}
