package nda.search.general.application.indexation

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import nda.search.general.application.exception.IndexationException
import nda.search.general.application.indexation.tp.runners.nda.AllKnowledgesFromNdaReceiverRunner
import nda.search.general.application.indexation.tp.runners.nda.BlocksFromNdaByBlockTranslationVersionRunner
import nda.search.general.application.indexation.tp.runners.nda.BlocksFromNdaByEmbeddedEntitiesRunner
import nda.search.general.application.indexation.tp.runners.nda.BlocksFromNdaByKnowledgeBlockLinkRunner
import nda.search.general.application.indexation.tp.runners.nda.KnowledgesFromNdaByKnowledgePropertyRunner
import nda.search.general.application.indexation.tp.runners.nda.KnowledgesFromNdaByKnowledgeRunner
import nda.search.general.domain.model.IndexationEntityType
import nda.search.general.domain.model.IndexationInfo
import nda.search.general.domain.model.IndexationTask
import nda.search.general.domain.model.requests.IndexationCreateRq
import nda.search.general.domain.model.requests.IndexationTaskStartRq
import nda.search.general.domain.model.requests.IndexationTasksCreateRq
import nda.search.general.domain.model.requests.IndexationTasksStopRq
import nda.search.general.domain.repository.IndexationInfoRepository
import nda.search.general.domain.repository.IndexationTaskRepository
import ru.nda.library.spring.boot.jdbc.postgres.cluster.starter.transaction.TransactionalHelper
import ru.nda.qe.tp.Task
import ru.nda.qe.tp.TaskService

@Service
class IndexationService @Autowired constructor(
    private val indexationInfoRepository: IndexationInfoRepository,
    private val indexationTaskRepository: IndexationTaskRepository,
    private val transactionalHelper: TransactionalHelper,

    private val taskService: TaskService,

    private val allKnowledgesFromNdaReceiverRunner: AllKnowledgesFromNdaReceiverRunner,

    private val blocksFromNdaByBlockTranslationVersionRunner: BlocksFromNdaByBlockTranslationVersionRunner,
    private val blocksFromNdaByEmbeddedEntitiesRunner: BlocksFromNdaByEmbeddedEntitiesRunner,
    private val blocksFromNdaByKnowledgeBlockLinkRunner: BlocksFromNdaByKnowledgeBlockLinkRunner,

    private val knowledgesFromNdaByKnowledgePropertyRunner: KnowledgesFromNdaByKnowledgePropertyRunner,
    private val knowledgesFromNdaByKnowledgeRunner: KnowledgesFromNdaByKnowledgeRunner
) {
    fun create(indexationCreateRq: IndexationCreateRq): IndexationInfo {
        return indexationInfoRepository.create(indexationCreateRq)
            ?: throw IndexationException.CannotCreateIndexationException()
    }

    fun getAllActive(): List<IndexationInfo> {
        return indexationInfoRepository.getAllActive()
    }

    fun get(indexationId: Long): IndexationInfo {
        return indexationInfoRepository.get(indexationId)
            ?: throw IndexationException.IndexationNotFoundException(indexationId)
    }

    fun updateIndexationEnable(indexationId: Long, newEnabled: Boolean): IndexationInfo {
        return transactionalHelper.tx("IndexationService.updateIndexationEnable") {
            val indexationInfo = indexationInfoRepository.updateIndexationEnable(indexationId, newEnabled)
                ?: throw IndexationException.IndexationNotFoundException(indexationId)

            when (newEnabled) {
                true -> startIndexation(indexationInfo)
                false -> stopIndexation(indexationInfo)
            }

            return@tx indexationInfo
        }
    }

    fun updateIndexationRunners(indexationId: Long): IndexationInfo {
        return transactionalHelper.tx("IndexationService.updateIndexationRunners") {
            val indexationInfo = indexationInfoRepository.get(indexationId)
                ?: throw IndexationException.IndexationNotFoundException(indexationId)

            val availableRunners = parseTaskRunners(indexationInfo.indexationEntityType)
            val currentRunners = indexationTaskRepository.getTasksByIndexationId(indexationId).map { it.runnerName }

            val missingRunners = availableRunners.filter { !currentRunners.contains(it) }
            startIndexation(indexationInfo, missingRunners)

            return@tx indexationInfo
        }
    }

    private fun startIndexation(indexationInfo: IndexationInfo, runners: List<String>? = null) {
        val indexationTasks = indexationTaskRepository.createTasks(
            IndexationTasksCreateRq(
                indexationId = indexationInfo.indexationId,
                startedTs = indexationInfo.lastIndexedEntityTs,
                runners = runners ?: parseTaskRunners(indexationInfo.indexationEntityType),
            )
        )

        val tpTaskByIndexationTask = indexationTasks.associateWith { buildTpTask(it) }
        indexationTaskRepository.startTasks(
            indexationTasks.map { IndexationTaskStartRq(it, tpTaskByIndexationTask[it]?.uid) }
        )

        taskService.submit(tpTaskByIndexationTask.values)
        LOG.debug("Successfully started tasks with uids ${tpTaskByIndexationTask.values.mapNotNull { it?.uid }}")
    }

    private fun parseTaskRunners(indexationEntityType: IndexationEntityType): List<String> {
        return when (indexationEntityType) {
            IndexationEntityType.BLOCK -> listOf(
                IndexationConstants.NdaRunner.BLOCKS_FROM_BLOCK_TRANSLATION_VERSION,
                IndexationConstants.NdaRunner.BLOCKS_FROM_EMBEDDED_ENTITIES,
                IndexationConstants.NdaRunner.BLOCKS_FROM_KNOWLEDGE_BLOCK_LINK,
            )

            IndexationEntityType.KNOWLEDGE -> listOf(
                IndexationConstants.NdaRunner.KNOWLEDGES_FROM_KNOWLEDGE_PROPERTY,
                IndexationConstants.NdaRunner.KNOWLEDGES_FROM_KNOWLEDGE,
            )

            IndexationEntityType.KNOWLEDGE_WITH_BLOCK -> listOf(IndexationConstants.NdaRunner.ALL_KNOWLEDGES)

            else -> emptyList()
        }
    }

    private fun buildTpTask(indexationTask: IndexationTask): Task? {
        val indexationId = indexationTask.indexationId

        return when (indexationTask.runnerName) {
            IndexationConstants.NdaRunner.ALL_KNOWLEDGES -> {
                allKnowledgesFromNdaReceiverRunner.newTask(indexationId, DEFAULT_RAG_INDEX)
            }

            IndexationConstants.NdaRunner.BLOCKS_FROM_BLOCK_TRANSLATION_VERSION -> {
                blocksFromNdaByBlockTranslationVersionRunner.newTask(indexationId)
            }

            IndexationConstants.NdaRunner.BLOCKS_FROM_EMBEDDED_ENTITIES -> {
                blocksFromNdaByEmbeddedEntitiesRunner.newTask(indexationId)
            }

            IndexationConstants.NdaRunner.BLOCKS_FROM_KNOWLEDGE_BLOCK_LINK -> {
                blocksFromNdaByKnowledgeBlockLinkRunner.newTask(indexationId)
            }

            IndexationConstants.NdaRunner.KNOWLEDGES_FROM_KNOWLEDGE_PROPERTY -> {
                knowledgesFromNdaByKnowledgePropertyRunner.newTask(indexationId)
            }

            IndexationConstants.NdaRunner.KNOWLEDGES_FROM_KNOWLEDGE -> {
                knowledgesFromNdaByKnowledgeRunner.newTask(indexationId)
            }

            else -> null
        }
    }

    private fun stopIndexation(indexationInfo: IndexationInfo) {
        val taskUids = indexationTaskRepository.getTaskUidsByIndexationId(indexationInfo.indexationId)
        taskService.cancel(taskUids)
        indexationTaskRepository.stopTasks(IndexationTasksStopRq(indexationInfo.indexationId))

        LOG.debug("Successfully stopped tasks with uids $taskUids")
    }

    companion object {
        val LOG = LoggerFactory.getLogger(IndexationService::class.java)

        const val DEFAULT_RAG_INDEX = -1L
    }
}
