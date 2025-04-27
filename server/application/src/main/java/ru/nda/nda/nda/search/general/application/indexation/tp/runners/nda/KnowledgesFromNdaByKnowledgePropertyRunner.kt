package nda.search.general.application.indexation.tp.runners.nda

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.nda.api.integration.searchgeneral.dto.index.IndexationSettingsRqDto
import nda.search.general.application.indexation.ConfigService
import nda.search.general.application.indexation.FilterService
import nda.search.general.application.indexation.IndexationConstants
import nda.search.general.application.indexation.IndexedKnowledgesService
import nda.search.general.application.indexation.RagService
import nda.search.general.application.indexation.model.nda.IndexingData
import nda.search.general.application.integration.nda.EntitiesFromNdaReceiverService
import nda.search.general.application.integration.rag.RagIndexationService
import nda.search.general.domain.repository.IndexationInfoRepository
import nda.search.general.domain.repository.IndexationTaskRepository
import nda.search.general.domain.repository.RagIndexRepository
import nda.search.general.domain.repository.RagSettingsRepository
import ru.nda.library.spring.boot.jdbc.postgres.cluster.starter.transaction.TransactionalHelper

@Component
class KnowledgesFromNdaByKnowledgePropertyRunner(
    override val entitiesFromNdaReceiverService: EntitiesFromNdaReceiverService,
    override val ragService: RagService,
    override val ragIndexationService: RagIndexationService,
    override val filterService: FilterService,
    override val configService: ConfigService,
    override val indexedKnowledgesService: IndexedKnowledgesService,

    @Value("\${nda.search.general.indexing.knowledge.by.knowledge.property.enabled:true}")
    override val isIndexingEnabled: Boolean,

    @Value("\${nda.search.general.indexing.knowledge.by.knowledge.property.reschedule.period:60}")
    override val reschedulePeriodSeconds: Long,

    @Value("\${nda.search.general.indexing.knowledge.by.knowledge.property.min.entity.age:60}")
    override val minEntityAge: Long,

    @Value("\${nda.search.general.indexing.knowledge.by.knowledge.property.max.range.seconds:600}")
    override val maxIndexingRangeSeconds: Long,

    @Value("\${nda.search.general.indexing.knowledge.by.knowledge.property.batch.size:30}")
    override val batchSize: Int,

    @Value("\${nda.search.general.indexing.knowledge.by.knowledge.property.max.batch.size:70}")
    override val maxBatchSize: Int,

    override val transactionalHelper: TransactionalHelper,
    override val indexationTaskRepository: IndexationTaskRepository,
    override val indexationInfoRepository: IndexationInfoRepository,
    override val ragIndexRepository: RagIndexRepository,
    override val ragSettingsRepository: RagSettingsRepository,
) : AbstractEntitiesFromNdaReceiverRunner() {
    override val runnerName: String
        get() = IndexationConstants.NdaRunner.KNOWLEDGES_FROM_KNOWLEDGE_PROPERTY

    override fun receiveIndexingDataFromNda(request: IndexationSettingsRqDto, indexationId: Long): IndexingData {
        val knowledgesForIndex = entitiesFromNdaReceiverService.getKnowledgesByKnowledgeProperty(request).batch
        LOG.debug("From nda receive ${knowledgesForIndex.size} knowledges for index")

        return convertToIndexingData(knowledgesForIndex, request, indexationId)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(KnowledgesFromNdaByKnowledgePropertyRunner::class.java)
    }
}
