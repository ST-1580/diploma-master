package nda.search.general.application.indexation.tp.runners.nda

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import ru.nda.api.integration.searchgeneral.dto.index.EntityFromNdaForIndexDto
import ru.nda.api.integration.searchgeneral.dto.index.IndexationByKnowledgeIdsSettingsRqDto
import ru.nda.api.integration.searchgeneral.dto.index.IndexationSettingsRqDto
import ru.nda.api.integration.searchgeneral.dto.index.KnowledgeWithBlocksForIndexDto
import ru.nda.api.v1.dto.EntityStatusDto
import nda.search.general.application.indexation.ConfigService
import nda.search.general.application.indexation.FilterService
import nda.search.general.application.indexation.IndexationConstants
import nda.search.general.application.indexation.IndexedKnowledgesService
import nda.search.general.application.indexation.RagService
import nda.search.general.application.indexation.configs.IndexationConfigService
import nda.search.general.application.indexation.model.nda.IndexingData
import nda.search.general.application.indexation.model.nda.IndexingSettingsInfo
import nda.search.general.application.integration.nda.EntitiesFromNdaReceiverService
import nda.search.general.application.integration.rag.RagIndexationService
import nda.search.general.domain.model.RagIndex
import nda.search.general.domain.model.requests.IndexationTaskUpdateRq
import nda.search.general.domain.repository.IndexationInfoRepository
import nda.search.general.domain.repository.IndexationTaskRepository
import nda.search.general.domain.repository.RagIndexRepository
import nda.search.general.domain.repository.RagSettingsRepository
import ru.nda.library.spring.boot.jdbc.postgres.cluster.starter.transaction.TransactionalHelper
import ru.nda.qe.tp.Context
import ru.nda.qe.tp.Task
import ru.nda.qe.tp.TaskBuilder
import ru.nda.qe.tp.TpRunner
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import kotlin.math.min

abstract class AbstractEntitiesFromNdaReceiverRunner : TpRunner<Any?> {
    private val poolName: String
        get() = IndexationConstants.PoolName.NDA_INDEXING_POOL

    protected abstract val runnerName: String
    protected abstract val isIndexingEnabled: Boolean

    protected abstract val reschedulePeriodSeconds: Long
    protected abstract val minEntityAge: Long
    protected abstract val maxIndexingRangeSeconds: Long
    protected abstract val batchSize: Int
    protected abstract val maxBatchSize: Int

    protected abstract val entitiesFromNdaReceiverService: EntitiesFromNdaReceiverService
    protected abstract val ragService: RagService
    protected abstract val ragIndexationService: RagIndexationService
    protected abstract val filterService: FilterService
    protected abstract val configService: ConfigService
    protected abstract val indexedKnowledgesService: IndexedKnowledgesService

    protected abstract val transactionalHelper: TransactionalHelper
    protected abstract val indexationTaskRepository: IndexationTaskRepository
    protected abstract val indexationInfoRepository: IndexationInfoRepository
    protected abstract val ragIndexRepository: RagIndexRepository
    protected abstract val ragSettingsRepository: RagSettingsRepository

    @Value("\${entities.from.nda.receiver.batch.incrementation.multiplier:1.4}")
    private lateinit var batchIncrementationMultiplier: String

    @Value("\${entities.from.nda.receiver.sleep.between.batches.seconds:0}")
    private lateinit var sleepBetweenBatchesSeconds: String

    @Value("\${nda.search.general.indexing.rag.service.name:test_in_prod}")
    lateinit var ragServiceName: String

    @Value("\${nda.search.general.indexing.rag.index.name.prefix:nda_test}")
    lateinit var ragIndexNamePrefix: String

    fun newTask(indexationId: Long): Task = TaskBuilder.builder()
        .setProperty(INDEXATION_ID, indexationId)
        .setRunner(runnerName)
        .setPool(poolName)
        .build()

    override fun getName(): String = runnerName

    override fun isEnabled(): Boolean = isIndexingEnabled

    override fun call(): Any? {
        val context = Context.get()
        try {
            doCall(context.getLongProperty(INDEXATION_ID))
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

    private fun doCall(indexationId: Long) {
        var hasMore = true
        var batchSizeForIteration = batchSize

        LOG.debug("Start indexing at runner $runnerName")
        while (hasMore) {
            val indexationTask = indexationTaskRepository.getTaskByIndexationIdAndRunnerName(
                indexationId = indexationId,
                runnerName = runnerName
            ) ?: throw IllegalArgumentException("No task for runnerName=$runnerName in indexation $indexationId")

            val toUpdatedTsOnNow = Instant.now().minusSeconds(minEntityAge).toEpochMilli()
            val toUpdatedTsOnLastIndexation = indexationTask.lastIndexedEntityTs + maxIndexingRangeSeconds * 1000
            val toUpdatedTs = min(toUpdatedTsOnNow, toUpdatedTsOnLastIndexation)

            val indexationSettings = IndexationSettingsRqDto(
                fromUpdatedTs = indexationTask.lastIndexedEntityTs,
                toUpdatedTs = toUpdatedTs,
                limit = batchSizeForIteration + 1,
                offset = indexationTask.entitiesWithIndexedTsCnt,
            )
            LOG.debug("Want to get entites with settings $indexationSettings")

            val dataForIndex = receiveIndexingDataFromNda(indexationSettings, indexationId)
            val (_, _, _, lastUpdatedTs, entitiesWithIndexedTsCnt, totalEntitiesInBatch) = dataForIndex

            val isAllDocsIndexed = indexEntities(dataForIndex)
            if (!isAllDocsIndexed) { break }

            val hasMoreData = totalEntitiesInBatch > batchSizeForIteration
            transactionalHelper.tx("AbstractBatchIndexingRunner.doCall") {
                (lastUpdatedTs ?: toUpdatedTs).let { newUpdatedTs ->
                    val isLastTimeEqStartTime = newUpdatedTs <= indexationTask.lastIndexedEntityTs

                    batchSizeForIteration =
                        if (isLastTimeEqStartTime && hasMoreData) {
                            minOf((batchIncrementationMultiplier.toDouble() * batchSizeForIteration).toInt(), maxBatchSize)
                        } else {
                            batchSize
                        }

                    val (lastIndexedEntityTs, newEntitiesWithIndexedTsCnt) =
                        if (isLastTimeEqStartTime && !hasMoreData) {
                            toUpdatedTs to 0
                        } else {
                            newUpdatedTs to (entitiesWithIndexedTsCnt ?: 0)
                        }

                    indexationTaskRepository.updateIndexationData(
                        indexationId = indexationId,
                        runnerName = runnerName,
                        indexationTaskUpdateRq = IndexationTaskUpdateRq(
                            lastIndexedEntityTs = lastIndexedEntityTs,
                            entitiesWithIndexedTsCnt = newEntitiesWithIndexedTsCnt
                        ),
                    )
                }

                val minTimeOfIndexation =
                    indexationTaskRepository.getTasksByIndexationId(indexationId).minOf { it.lastIndexedEntityTs }

                indexationInfoRepository.updateIndexationLastTs(indexationId, minTimeOfIndexation)
            }

            LOG.debug("Successfully receive data from nda and send to indexers")

            hasMore = (toUpdatedTsOnLastIndexation < toUpdatedTsOnNow) || hasMoreData

            if (hasMore && sleepBetweenBatchesSeconds != "0") {
                val timeForSleepSeconds = sleepBetweenBatchesSeconds.toLongOrNull() ?: continue
                Thread.sleep(timeForSleepSeconds * 1000)
            }
        }
    }

    protected abstract fun receiveIndexingDataFromNda(request: IndexationSettingsRqDto, indexationId: Long): IndexingData

    fun convertToIndexingData(
        entitiesFromNdaToIndex: List<EntityFromNdaForIndexDto>,
        request: IndexationSettingsRqDto,
        indexationId: Long,
    ): IndexingData {
        val (maxTs, entitiesWithIndexedTsCnt) = calculateMaxAndNewOffset(entitiesFromNdaToIndex.map { it.triggeredUpdatedTs }, request)
        val linkedRagIndexIds = indexationInfoRepository.getLinkedRagIndexIds(indexationId).toSet()

        val ragSettingsBySettingsId = ragSettingsRepository.getActiveIndexSettings().associateBy { it.id }
        val availableRagSettingsBySettingsId = ragSettingsBySettingsId
            .filter { if (linkedRagIndexIds.isNotEmpty()) linkedRagIndexIds.contains(it.value.ragIndexId) else true }
        val filterIdBySettingsId = availableRagSettingsBySettingsId.mapValues { it.value.filterId }

        val settingsIdsByKnowledgeId = getEntitySettings(filterIdBySettingsId, entitiesFromNdaToIndex)
        val knowledgeIdsForRag = settingsIdsByKnowledgeId.map { it.key }
        LOG.debug("Current settingsIdsByKnowledgeId: $settingsIdsByKnowledgeId")

        val fullKnowledges = entitiesFromNdaReceiverService.getKnowledgesWithBlocksByKnowledgeIds(
            IndexationByKnowledgeIdsSettingsRqDto(knowledgeIds = knowledgeIdsForRag)
        )
        LOG.debug("From nda receive following knowledgeWithBlocks: ${fullKnowledges.batch.map { it.knowledge.knowledgeId }}")

        val knowledgeIdsBySettingsId = splitBySettingsId(
            knowledges = fullKnowledges.batch,
            settingsIdsByKnowledgeId = settingsIdsByKnowledgeId,
        )
        LOG.debug("Current knowledgeIdsBySettingsId for indexation $knowledgeIdsBySettingsId")

        val outsideFilterKnowledgeIdsBySettingId = antiSplitBySettingsId(
            allKnowledgeIds = entitiesFromNdaToIndex.map { it.knowledgeId },
            allSettingIds = availableRagSettingsBySettingsId.keys.toList(),
            settingsIdsByKnowledgeId = settingsIdsByKnowledgeId,
        )
        LOG.debug("Current outsideFilterKnowledgeIdsBySettingId for indexation $outsideFilterKnowledgeIdsBySettingId")

        val (entitiesToIndex, entitiesToDelete) = fullKnowledges.batch.partition { it.knowledge.status == EntityStatusDto.ACTIVE }
        LOG.debug("From nda to indexation ${entitiesToIndex.size} knowledgeWithBlocks, to delete ${entitiesToDelete.size} knowledgeWithBlocks")

        return IndexingData(
            indexRequests = entitiesToIndex,
            deleteRequests = entitiesToDelete,
            lastUpdatedTs = maxTs,
            entitiesWithIndexedTsCnt = entitiesWithIndexedTsCnt,
            totalEntitiesInBatch = entitiesFromNdaToIndex.size,
            indexingSettingsInfo = IndexingSettingsInfo(
                knowledgeIdsBySettingsId = knowledgeIdsBySettingsId,
                ragSettingsBySettingsId = ragSettingsBySettingsId,
                outsideFilterKnowledgeIdsBySettingId = outsideFilterKnowledgeIdsBySettingId,
            )
        )
    }

    private fun getEntitySettings(
        filterIdBySettingsId: Map<Long, Long>,
        entitiesFromNdaToIndex: List<EntityFromNdaForIndexDto>
    ): Map<Long, List<Long>> {
        val filterFunctionBySettingId = filterService.getFilterFunctions(filterIdBySettingsId)

        return entitiesFromNdaToIndex
            .map {
                val settingsIdsToIndex = mutableListOf<Long>()
                for ((settingsId, filterFunc) in filterFunctionBySettingId) {
                    if (filterFunc.invoke(it)) {
                        settingsIdsToIndex.add(settingsId)
                    }
                }
                it to settingsIdsToIndex
            }
            .groupBy({ it.first.knowledgeId }, { it.second })
            .mapValues { it.value.flatten().distinct() }
            .filter { it.value.isNotEmpty() }
    }

    private fun indexEntities(indexingData: IndexingData): Boolean {
        var isAllDocsIndexed = true

        val ragIndexIds = indexingData.indexingSettingsInfo.ragSettingsBySettingsId.map { it.value.ragIndexId }.distinct()
        val ragIndexByRagIndexId = ragIndexRepository.getRagIndexByIndexId(ragIndexIds = ragIndexIds)

        val configIds = indexingData.indexingSettingsInfo.ragSettingsBySettingsId.map { it.value.configId }.distinct()
        val configsByConfigId = configService.getConfigsByConfigId(configIds)
        val configClassByConfigId = configService.getConfigClassByConfigId(configsByConfigId)
        LOG.debug("Found following configsByConfigId: $configsByConfigId")

        for ((settingsId, _) in indexingData.indexingSettingsInfo.ragSettingsBySettingsId) {
            if (!isAllDocsIndexed) {
                LOG.debug("Current indexation stops because of error during indexing")
                break
            }

            val underFilterKnowledgeIds =
                indexingData.indexingSettingsInfo.knowledgeIdsBySettingsId[settingsId] ?: emptyList()
            val outsideFilterKnowledgeIds =
                indexingData.indexingSettingsInfo.outsideFilterKnowledgeIdsBySettingId[settingsId] ?: emptyList()
            if (underFilterKnowledgeIds.isEmpty() && outsideFilterKnowledgeIds.isEmpty()) {
                LOG.debug("Empty candidates with settings id=$settingsId. Do nothing")
                continue
            }

            val ragIndexId = indexingData.indexingSettingsInfo.ragSettingsBySettingsId[settingsId]?.ragIndexId ?: continue
            LOG.debug(
                "With settings id=$settingsId to $ragIndexId ragIndex found following candidates: " +
                "underFilterKnowledgeIds=$underFilterKnowledgeIds, outsideFilterKnowledgeIds=$outsideFilterKnowledgeIds"
            )

            val configId = indexingData.indexingSettingsInfo.ragSettingsBySettingsId[settingsId]?.configId ?: continue

            val (ok, indexedDocIds) = indexToRag(
                indexRequests = indexingData.indexRequests,
                underFilterKnowledgeIds = underFilterKnowledgeIds,
                configClass = configClassByConfigId[configId] ?: continue,
                ragIndex = ragIndexByRagIndexId[ragIndexId] ?: continue,
            )
            if (!ok) {
                isAllDocsIndexed = false
                continue
            }

            isAllDocsIndexed = deleteFromRag(
                deleteRequests = indexingData.deleteRequests,
                underFilterKnowledgeIds = underFilterKnowledgeIds,
                outsideFilterKnowledgeIds = outsideFilterKnowledgeIds,
                indexedDocIds = indexedDocIds,
                ragIndex = ragIndexByRagIndexId[ragIndexId] ?: continue,
            )
        }

        return isAllDocsIndexed
    }

    private fun splitBySettingsId(
        knowledges: List<KnowledgeWithBlocksForIndexDto>,
        settingsIdsByKnowledgeId: Map<Long, List<Long>>,
    ): Map<Long, List<Long>> {
        return knowledges
            .flatMap {
                val knowledgeId = it.knowledge.knowledgeId
                val settingsIds = settingsIdsByKnowledgeId[knowledgeId] ?: return@flatMap emptyList()
                settingsIds.map { settingsId -> settingsId to knowledgeId }
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { it.value.distinct() }
    }

    private fun antiSplitBySettingsId(
        allKnowledgeIds: List<Long>,
        allSettingIds: List<Long>,
        settingsIdsByKnowledgeId: Map<Long, List<Long>>,
    ): Map<Long, List<Long>> {
        return allKnowledgeIds
            .flatMap {
                val settingsIds = settingsIdsByKnowledgeId[it] ?: emptyList()
                val outsideSettingIds = allSettingIds - settingsIds
                outsideSettingIds.map { settingsId -> settingsId to it }
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { it.value.distinct() }
    }

    private fun indexToRag(
        indexRequests: List<KnowledgeWithBlocksForIndexDto>,
        underFilterKnowledgeIds: List<Long>,
        configClass: IndexationConfigService,
        ragIndex: RagIndex,
    ) : Pair<Boolean, Set<String>> {
        val indexDocs = indexRequests.filter { underFilterKnowledgeIds.contains(it.knowledge.knowledgeId) }
        val indexBatch = configClass.getIndexBatch(indexDocs)
        LOG.debug("Want to send for index ${indexBatch.size} documents with docIds ${indexBatch.map { it.docId }}")

        if (indexBatch.isNotEmpty()) {
            try {
                val ragIndexInfo = ragService.indexDocuments(
                    ragIndex = ragIndex,
                    documents = indexBatch
                )

                LOG.debug("Successfully indexed knowledgesWithBlocks to RAG with response $ragIndexInfo")
            } catch (e: Exception) {
                LOG.error("Error during send index request to rag", e)
                return false to emptySet()
            }
        }

        return true to indexBatch.map { it.docId }.toSet()
    }

    private fun deleteFromRag(
        deleteRequests: List<KnowledgeWithBlocksForIndexDto>,
        underFilterKnowledgeIds: List<Long>,
        outsideFilterKnowledgeIds: List<Long>,
        indexedDocIds: Set<String>,
        ragIndex: RagIndex,
    ): Boolean {
        val fullDeleteKnowledgeIds = deleteRequests
            .filter { underFilterKnowledgeIds.contains(it.knowledge.knowledgeId) }
            .map { it.knowledge.knowledgeId }

        val deleteByFullDeleteKnowledgeIds = indexedKnowledgesService.getRagDocsByKnowledgeId(ragIndex.id, fullDeleteKnowledgeIds)
        val deleteByOutsideFilterKnowledgeIds = indexedKnowledgesService.getRagDocsByKnowledgeId(ragIndex.id, outsideFilterKnowledgeIds)
        val deleteByDiff = indexedKnowledgesService.getRagDocsByKnowledgeId(ragIndex.id, underFilterKnowledgeIds - fullDeleteKnowledgeIds)
                .filter { !indexedDocIds.contains(it.docId) }

        val deleteBatch = deleteByFullDeleteKnowledgeIds + deleteByOutsideFilterKnowledgeIds + deleteByDiff

        LOG.debug(
            "Want to send for delete ${deleteBatch.size} documents:" +
                " fullDeleteKnowledgeIds size = ${deleteByFullDeleteKnowledgeIds.size} values = $deleteByFullDeleteKnowledgeIds," +
                " outsideFilterKnowledgeIds size = ${deleteByOutsideFilterKnowledgeIds.size} values = $deleteByOutsideFilterKnowledgeIds," +
                " diff size = ${deleteByDiff.size} values = $deleteByDiff"
        )

        if (deleteBatch.isNotEmpty()) {
            try {
                val ragIndexInfo = ragService.deleteDocuments(
                    ragIndex = ragIndex,
                    documents = deleteBatch,
                )

                LOG.debug("Successfully deleted knowledgesWithBlocks from RAG with response $ragIndexInfo")
            } catch (e: Exception) {
                LOG.error("Error during send delete request to rag", e)
                return false
            }
        }

        return true
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(AbstractEntitiesFromNdaReceiverRunner::class.java)

        const val INDEXATION_ID = "indexation_id"
        const val NOT_EXISTS_RAG_INDEX_ID = -1L

        fun calculateMaxAndNewOffset(entities: List<Long>, request: IndexationSettingsRqDto): Pair<Long?, Int?> {
            val maxTs = entities.maxOfOrNull { it }
            val entitiesWithIndexedTsCnt = if (maxTs != null) {
                if (maxTs == request.fromUpdatedTs) {
                    request.offset + entities.size
                } else {
                    entities.count { it == maxTs }
                }
            } else {
                null
            }

            return maxTs to entitiesWithIndexedTsCnt
        }
    }
}
