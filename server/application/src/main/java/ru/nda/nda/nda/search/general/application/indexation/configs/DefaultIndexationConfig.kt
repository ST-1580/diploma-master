package nda.search.general.application.indexation.configs

import org.slf4j.LoggerFactory
import ru.nda.api.integration.searchgeneral.dto.index.KnowledgeWithBlocksForIndexDto
import nda.search.general.application.indexation.dto.rag.index.RagDocumentForIndexDto

data object DefaultIndexationConfig : IndexationConfigService {
    override fun getIndexBatch(knowledges: List<KnowledgeWithBlocksForIndexDto>): List<RagDocumentForIndexDto> {
        LOG.debug("Index batch via DEFAULT_CONFIG preparing")
        TODO("Not yet implemented")
    }

    private val LOG = LoggerFactory.getLogger(DefaultIndexationConfig::class.java)
}
