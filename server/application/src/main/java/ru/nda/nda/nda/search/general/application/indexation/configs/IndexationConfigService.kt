package nda.search.general.application.indexation.configs

import ru.nda.api.integration.searchgeneral.dto.index.BlockEmbeddedEntityForIndexDto
import ru.nda.api.integration.searchgeneral.dto.index.KnowledgeWithBlocksForIndexDto
import nda.search.general.application.indexation.dto.rag.index.RagDocumentForIndexDto

sealed interface IndexationConfigService {
    fun getIndexBatch(knowledges: List<KnowledgeWithBlocksForIndexDto>): List<RagDocumentForIndexDto>
}
