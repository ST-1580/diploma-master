package nda.search.general.application.indexation.model.nda

import ru.nda.api.integration.searchgeneral.dto.index.KnowledgeWithBlocksForIndexDto

data class IndexingData(
    val indexRequests: List<KnowledgeWithBlocksForIndexDto>,
    val deleteRequests: List<KnowledgeWithBlocksForIndexDto>,
    val indexingSettingsInfo: IndexingSettingsInfo,
    val lastUpdatedTs: Long?,
    val entitiesWithIndexedTsCnt: Int?,
    val totalEntitiesInBatch: Int,
)
