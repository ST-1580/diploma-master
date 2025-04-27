package nda.search.general.application.indexation.tp.runners.nda

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.nda.api.integration.searchgeneral.dto.index.ActiveKnowledgeForIndexDto
import ru.nda.api.integration.searchgeneral.dto.index.AllKnowledgesForIndexationSettingsRqDto
import ru.nda.api.integration.searchgeneral.dto.index.IndexationByKnowledgeIdsSettingsRqDto
import nda.search.general.application.indexation.IndexationConstants
import nda.search.general.application.indexation.dto.rag.index.RagDocumentForIndexBatchDto
import nda.search.general.application.indexation.dto.rag.index.RagSwitchVersionRqDto
import nda.search.general.application.indexation.model.nda.toRagIndexDto
import nda.search.general.application.indexation.model.rag.RagIndexationInfo
import nda.search.general.application.integration.nda.EntitiesFromNdaReceiverService
import nda.search.general.application.integration.rag.RagIndexationService
import nda.search.general.domain.model.requests.IndexationTaskUpdateRq
import nda.search.general.domain.repository.IndexationInfoRepository
import nda.search.general.domain.repository.IndexationTaskRepository
import nda.search.general.domain.repository.RagIndexRepository
import ru.nda.library.spring.boot.jdbc.postgres.cluster.starter.transaction.TransactionalHelper
import ru.nda.qe.tp.Context
import ru.nda.qe.tp.Task
import ru.nda.qe.tp.TaskBuilder
import ru.nda.qe.tp.TpRunner
import java.time.Clock
import java.time.LocalDateTime
import java.util.concurrent.Executors

@Component
class AllKnowledgesFromNdaReceiverRunner(
    private val entitiesFromNdaReceiverService: EntitiesFromNdaReceiverService,
    private val ragIndexationService: RagIndexationService,

    @Value("\${nda.search.general.indexing.all.knowledges.enabled:true}")
    private val isIndexingEnabled: Boolean,

    @Value("\${nda.search.general.indexing.all.knowledges.reschedule.period:300}")
    private val reschedulePeriodSeconds: Long,

    @Value("\${nda.search.general.indexing.all.knowledges.max.batch.size:1400}")
    private val maxBatchSize: Int,

    @Value("\${nda.search.general.indexing.all.knowledges.many.batches.enabled:false}")
    private val manyBatchesEnabled: Boolean,

    @Value("\${nda.search.general.indexing.all.knowledges.pool.size:32}")
    private val poolSize: Int,

    @Value("\${nda.search.general.indexing.rag.service.name:test_in_prod}")
    private val ragServiceName: String,

    @Value("\${nda.search.general.indexing.rag.index.name.prefix:nda_test}")
    private val ragIndexNamePrefix: String,

    private val transactionalHelper: TransactionalHelper,
    private val indexationTaskRepository: IndexationTaskRepository,
    private val indexationInfoRepository: IndexationInfoRepository,
    private val ragIndexRepository: RagIndexRepository,
) : TpRunner<Any?> {
    private val poolName: String
        get() = IndexationConstants.PoolName.NDA_INDEXING_POOL

    private val runnerName: String
        get() = IndexationConstants.NdaRunner.ALL_KNOWLEDGES

    private val dispatcher: CoroutineDispatcher = Executors.newFixedThreadPool(poolSize).asCoroutineDispatcher()

    fun newTask(indexationId: Long, ragIndexId: Long): Task = TaskBuilder.builder()
        .setProperty(INDEXATION_ID, indexationId)
        .setProperty(RAG_INDEX_ID, ragIndexId)
        .setRunner(runnerName)
        .setPool(poolName)
        .build()

    override fun getName(): String = runnerName

    override fun isEnabled(): Boolean = isIndexingEnabled

    override fun call(): Any? {
        val context = Context.get()
        try {
            doCall(
                indexationId = context.getLongProperty(INDEXATION_ID),
                ragIndexId = context.getLongProperty(RAG_INDEX_ID)
            )
        } catch (e: Exception) {
            LOG.error("Task failed on pool $poolName, on runner $runnerName", e)
        }

        val scheduledTime = LocalDateTime
            .now(Clock.systemUTC())
            .plusSeconds(reschedulePeriodSeconds)

        context.reschedule(
            "Runner $runnerName is waiting for next run on $scheduledTime",
            scheduledTime
        )

        return null
    }

    private fun doCall(indexationId: Long, ragIndexId: Long) {
        LOG.debug("Start indexing at runner $runnerName")

        // only check
        indexationTaskRepository.getTaskByIndexationIdAndRunnerName(
            indexationId = indexationId,
            runnerName = runnerName
        ) ?: throw IllegalArgumentException("No task for runnerName=$runnerName in indexation $indexationId")

        val ragIndex = ragIndexRepository.getById(ragIndexId)
        LOG.debug("Index to ragIndex=$ragIndex")

        val activeKnowledges = entitiesFromNdaReceiverService.getAllActiveKnowledges(
            AllKnowledgesForIndexationSettingsRqDto(
                targetClusterIds = ragIndex.ndaClusterIds,
            ),
        )
        LOG.debug("Want to index ${activeKnowledges.batch.size} knowledges")

        val batches = activeKnowledges.batch.chunked(maxBatchSize)
        if (batches.isEmpty()) {
            LOG.debug("Zero batches for index. Do nothing")
            updateRunnerTs(indexationId, 0)
            return
        }

        val batchesForIndex = if (manyBatchesEnabled) { batches } else { listOf(batches.first()) }
        val totalBatchesForIndex = batchesForIndex.size
        LOG.debug("Ready to index $totalBatchesForIndex of ${batches.size} batches")

        val indexName = "${ragIndexNamePrefix}_$ragIndexId"
        val productName = ragIndex.productName

        val firstBatch = batchesForIndex.first()
        val firstBatchSendResult = sendToRag(
            batch = firstBatch,
            indexName = indexName,
            productName = productName
        )
        LOG.debug("Send to rag batch 1 of $totalBatchesForIndex with result $firstBatchSendResult")

        val newIndexVersion = firstBatchSendResult.indexVersion
        var canSwitch = firstBatchSendResult.canSwitch
        var sendDocsCnt = firstBatchSendResult.indexedDocsCnt

        var index = 1
        while (canSwitch && index < totalBatchesForIndex) {
            val sendResult = sendToRag(
                batch = batchesForIndex[index],
                indexVersion = newIndexVersion,
                indexName = indexName,
                productName = productName
            )

            LOG.debug("Send to rag batch ${index + 2} of $totalBatchesForIndex with result $firstBatchSendResult")

            canSwitch = sendResult.canSwitch
            sendDocsCnt += sendDocsCnt
            index++
        }

        if (canSwitch) {
            LOG.debug("Successfully indexed all knowledgesWithBlocks to RAG. Want to change actual index version to $newIndexVersion")

            ragIndexationService.switchIndexVersion(
                indexName = indexName,
                rq = RagSwitchVersionRqDto(
                    service = ragServiceName,
                    product = productName,
                    indexVersion = newIndexVersion,
                )
            )

            LOG.debug("Actual index version in RAG switched to $newIndexVersion")

            updateRunnerTs(indexationId, sendDocsCnt)
        }
    }

    private fun sendToRag(
        batch: List<ActiveKnowledgeForIndexDto>,
        indexVersion: Long? = null,
        indexName: String,
        productName: String,
    ): RagIndexationInfo {
        val knowledgesWithBlocks = entitiesFromNdaReceiverService.getKnowledgesWithBlocksByKnowledgeIds(
            IndexationByKnowledgeIdsSettingsRqDto(
                knowledgeIds = batch.map { it.knowledgeId }
            )
        )

        LOG.debug("Ready ${knowledgesWithBlocks.batch.size} knowledgesWithBlocks for indexation to RAG")

        val ragRequest = RagDocumentForIndexBatchDto(
            service = ragServiceName,
            product = productName,
            indexVersion = indexVersion,
            autoSwitch = false,
            documents = knowledgesWithBlocks.batch.map(::toRagIndexDto)
        )

        try {
            val ragIndexInfo = ragIndexationService.indexDocuments(
                indexName = indexName,
                documents = ragRequest
            )

            LOG.debug("Successfully indexed knowledgesWithBlocks to RAG with response $ragIndexInfo")

            return RagIndexationInfo(
                canSwitch = true,
                indexVersion = ragIndexInfo.indexVersion,
                indexedDocsCnt = knowledgesWithBlocks.batch.size,
            )
        } catch (e: Exception) {
            LOG.error("Error during send documents to RAG", e)

            return RagIndexationInfo(canSwitch = false)
        }
    }

    private fun updateRunnerTs(indexationId: Long, sendDocsCnt: Int) {
        transactionalHelper.tx("AllKnowledgesFromNdaReceiverRunner.doCall") {
            val endTime = Clock.systemUTC().millis()

            indexationTaskRepository.updateIndexationData(
                indexationId = indexationId,
                runnerName = runnerName,
                indexationTaskUpdateRq = IndexationTaskUpdateRq(
                    lastIndexedEntityTs = endTime,
                    entitiesWithIndexedTsCnt = sendDocsCnt,
                ),
            )

            indexationInfoRepository.updateIndexationLastTs(indexationId, endTime)
        }
    }

    companion object {
        val LOG = LoggerFactory.getLogger(AllKnowledgesFromNdaReceiverRunner::class.java)

        const val INDEXATION_ID = "indexation_id"
        const val RAG_INDEX_ID = "rag_index_id"
    }
}
